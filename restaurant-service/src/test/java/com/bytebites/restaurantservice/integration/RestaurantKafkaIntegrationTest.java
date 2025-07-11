package com.bytebites.restaurantservice.integration;

import com.bytebites.restaurantservice.event.RestaurantCreatedEvent;
import com.bytebites.restaurantservice.repository.RestaurantRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RestaurantKafkaIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private String baseUrl;
    private Consumer<String, RestaurantCreatedEvent> consumer;

    @BeforeEach
    void setUpEach() {
        baseUrl = "http://localhost:" + port;

        
        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, "com.bytebites.restaurant.event",
                JsonDeserializer.VALUE_DEFAULT_TYPE, RestaurantCreatedEvent.class
        );

        consumer = new DefaultKafkaConsumerFactory<String, RestaurantCreatedEvent>(consumerProps).createConsumer();
        consumer.subscribe(Collections.singletonList("restaurant-events"));
    }

    @Test
    void shouldPublishRestaurantCreatedEventWhenRestaurantIsCreated() throws Exception {
        
        String jwt = generateSimpleJWT();
        HttpHeaders headers = createAuthHeaders(jwt);

        String requestBody = """
                {
                    "name": "Kafka Test Restaurant",
                    "description": "Testing Kafka events",
                    "address": "123 Kafka St",
                    "phone": "+1234567890",
                    "email": "kafka@restaurant.com"
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

        
        ConsumerRecords<String, RestaurantCreatedEvent> records = consumer.poll(Duration.ofSeconds(10));
        assertFalse(records.isEmpty());

        RestaurantCreatedEvent event = records.iterator().next().value();
        assertNotNull(event);
        assertEquals("RestaurantCreated", event.eventType());
        assertEquals("Kafka Test Restaurant", event.restaurantName());
        assertEquals("123 Kafka St", event.address());
        assertNotNull(event.eventId());
        assertNotNull(event.timestamp());
    }

    @Test
    void shouldNotPublishEventWhenRestaurantCreationFails() throws Exception {
        
        String jwt = generateSimpleJWT();
        HttpHeaders headers = createAuthHeaders(jwt);

        
        String requestBody = """
                {
                    "name": "",
                    "description": ""
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

        
        ConsumerRecords<String, RestaurantCreatedEvent> records = consumer.poll(Duration.ofSeconds(5));
        assertTrue(records.isEmpty());
    }

    private String generateSimpleJWT() {
        
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }

    private HttpHeaders createAuthHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwt);
        headers.set("X-User-Id", UUID.randomUUID().toString());
        headers.set("X-User-Email", "test@example.com");
        headers.set("X-User-Roles", "ROLE_RESTAURANT_OWNER");
        return headers;
    }
}