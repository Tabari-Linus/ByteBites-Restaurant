package com.bytebites.restaurantservice.integration;


import com.bytebites.restaurantservice.enums.RestaurantStatus;
import com.bytebites.restaurantservice.model.Restaurant;
import com.bytebites.restaurantservice.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RestaurantSecurityIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private UUID restaurantOwnerId;
    private UUID customerId;
    private UUID adminId;
    private Restaurant testRestaurant;

    @BeforeEach
    void setUpEach() {
        baseUrl = "http://localhost:" + port;
        restaurantOwnerId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        testRestaurant = new Restaurant();
        testRestaurant.setOwnerId(restaurantOwnerId);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setDescription("Test Description");
        testRestaurant.setAddress("123 Test St");
        testRestaurant.setPhone("+1234567890");
        testRestaurant.setEmail("test@restaurant.com");
        testRestaurant.setStatus(RestaurantStatus.ACTIVE);
        testRestaurant = restaurantRepository.save(testRestaurant);
    }

    @Test
    void shouldAllowRestaurantOwnerToCreateRestaurant() {
        
        String jwt = generateJWT(restaurantOwnerId, List.of("ROLE_RESTAURANT_OWNER"));
        HttpHeaders headers = createAuthHeaders(jwt);

        String requestBody = """
                {
                    "name": "New Restaurant",
                    "description": "New Description",
                    "address": "456 New St",
                    "phone": "+9876543210",
                    "email": "new@restaurant.com"
                }
                """;

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/restaurants",
                HttpMethod.POST,
                request,
                String.class
        );

        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("New Restaurant"));
    }


    @Test
    void shouldAllowOwnerToUpdateOwnRestaurant() {
        
        String jwt = generateJWT(restaurantOwnerId, List.of("ROLE_RESTAURANT_OWNER"));
        HttpHeaders headers = createAuthHeaders(jwt);

        String requestBody = """
                {
                    "name": "Updated Restaurant Name",
                    "description": "Updated Description"
                }
                """;

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/restaurants/" + testRestaurant.getId(),
                HttpMethod.PUT,
                request,
                String.class
        );

        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Updated Restaurant Name"));
    }


    @Test
    void shouldAllowAdminToUpdateAnyRestaurant() {

        String jwt = generateJWT(adminId, List.of("ROLE_ADMIN"));
        HttpHeaders headers = createAuthHeaders(jwt);

        String requestBody = """
                {
                    "name": "Admin Updated Restaurant",
                    "description": "Updated by admin"
                }
                """;

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);


        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/restaurants/" + testRestaurant.getId(),
                HttpMethod.PUT,
                request,
                String.class
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Admin Updated Restaurant"));
    }



    private String generateJWT(UUID userId, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor("mySecretKeyThatIsAtLeast256BitsLongForHMACSHA256Algorithm".getBytes());

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", "test@example.com")
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
    }

    private HttpHeaders createAuthHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwt);
        headers.set("X-User-Id", restaurantOwnerId.toString());
        headers.set("X-User-Email", "test@example.com");
        headers.set("X-User-Roles", "ROLE_RESTAURANT_OWNER");
        return headers;
    }
}
