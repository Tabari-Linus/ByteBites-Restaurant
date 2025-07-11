package com.bytebites.restaurantservice.integration;

import com.bytebites.restaurantservice.enums.RestaurantStatus;
import com.bytebites.restaurantservice.model.Restaurant;
import com.bytebites.restaurantservice.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RestaurantApiIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private UUID ownerId;

    @BeforeEach
    void setUpEach() {
        baseUrl = "http://localhost:" + port;
        ownerId = UUID.randomUUID();
    }

    @Test
    void shouldCreateRestaurantWithValidData() {

        HttpHeaders headers = createValidAuthHeaders();
        String requestBody = """
                {
                    "name": "Integration Test Restaurant",
                    "description": "Created via integration test",
                    "address": "123 Integration St",
                    "phone": "+1234567890",
                    "email": "integration@test.com"
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

        String responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Integration Test Restaurant", JsonPath.read(responseBody, "$.name"));
        assertEquals("PENDING_APPROVAL", JsonPath.read(responseBody, "$.status"));
        assertNotNull(JsonPath.read(responseBody, "$.id"));
        assertNotNull(JsonPath.read(responseBody, "$.createdAt"));
    }

    @Test
    void shouldValidateRequiredFields() {

        HttpHeaders headers = createValidAuthHeaders();
        String requestBody = """
                {
                    "description": "Missing required fields"
                }
                """;

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);


        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/restaurants",
                HttpMethod.POST,
                request,
                String.class
        );


        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("validation"));
    }

    @Test
    void shouldGetAllActiveRestaurants() {

        createTestRestaurant("Active Restaurant 1", RestaurantStatus.ACTIVE);
        createTestRestaurant("Active Restaurant 2", RestaurantStatus.ACTIVE);
        createTestRestaurant("Inactive Restaurant", RestaurantStatus.INACTIVE);


        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/restaurants",
                String.class
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);


        Integer count = JsonPath.read(responseBody, "$.length()");
        assertEquals(2, count);
    }


    @Test
    void shouldUpdateRestaurant() {

        Restaurant restaurant = createTestRestaurant("Original Name", RestaurantStatus.ACTIVE);
        HttpHeaders headers = createValidAuthHeaders();

        String requestBody = """
                {
                    "name": "Updated Name",
                    "description": "Updated Description"
                }
                """;

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);


        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/restaurants/" + restaurant.getId(),
                HttpMethod.PUT,
                request,
                String.class
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Updated Name", JsonPath.read(responseBody, "$.name"));
        assertEquals("Updated Description", JsonPath.read(responseBody, "$.description"));
    }

    @Test
    void shouldDeleteRestaurant() {

        Restaurant restaurant = createTestRestaurant("To Delete", RestaurantStatus.ACTIVE);
        HttpHeaders headers = createValidAuthHeaders();

        HttpEntity<Void> request = new HttpEntity<>(headers);


        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/restaurants/" + restaurant.getId(),
                HttpMethod.DELETE,
                request,
                Void.class
        );


        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());


        Restaurant deleted = restaurantRepository.findById(restaurant.getId()).orElse(null);
        assertNotNull(deleted);
        assertEquals(RestaurantStatus.INACTIVE, deleted.getStatus());
    }

    @Test
    void shouldGetOwnerRestaurants() {

        createTestRestaurant("Owner Restaurant 1", RestaurantStatus.ACTIVE);
        createTestRestaurant("Owner Restaurant 2", RestaurantStatus.PENDING_APPROVAL);


        Restaurant otherOwnerRestaurant = new Restaurant();
        otherOwnerRestaurant.setOwnerId(UUID.randomUUID());
        otherOwnerRestaurant.setName("Other Owner Restaurant");
        otherOwnerRestaurant.setDescription("Not mine");
        otherOwnerRestaurant.setAddress("456 Other St");
        otherOwnerRestaurant.setPhone("+9876543210");
        otherOwnerRestaurant.setStatus(RestaurantStatus.ACTIVE);
        restaurantRepository.save(otherOwnerRestaurant);

        HttpHeaders headers = createValidAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);


        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/restaurants/my",
                HttpMethod.GET,
                request,
                String.class
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertNotNull(responseBody);


        Integer count = JsonPath.read(responseBody, "$.length()");
        assertEquals(2, count);
    }

    private Restaurant createTestRestaurant(String name, RestaurantStatus status) {
        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerId(ownerId);
        restaurant.setName(name);
        restaurant.setDescription("Test description");
        restaurant.setAddress("123 Test St");
        restaurant.setPhone("+1234567890");
        restaurant.setEmail("test@example.com");
        restaurant.setStatus(status);
        return restaurantRepository.save(restaurant);
    }

    private HttpHeaders createValidAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("valid.jwt.token");
        headers.set("X-User-Id", ownerId.toString());
        headers.set("X-User-Email", "test@example.com");
        headers.set("X-User-Roles", "ROLE_RESTAURANT_OWNER");
        return headers;
    }
}