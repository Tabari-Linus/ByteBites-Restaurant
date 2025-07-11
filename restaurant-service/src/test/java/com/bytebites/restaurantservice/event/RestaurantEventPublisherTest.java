package com.bytebites.restaurantservice.event;

import com.bytebites.restaurantservice.enums.RestaurantStatus;
import com.bytebites.restaurantservice.model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.when;

@Timeout(10)
class RestaurantEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private RestaurantEventPublisher restaurantEventPublisher;

    private final String restaurantEventsTopic = "restaurant-events";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restaurantEventPublisher = new RestaurantEventPublisher(kafkaTemplate, restaurantEventsTopic);
    }

    @Test
    void testConstructorWithKafkaTemplateAndTopic() {

        KafkaTemplate<String, Object> mockKafkaTemplate = mock(KafkaTemplate.class);
        String topic = "test-topic";

        RestaurantEventPublisher publisher = new RestaurantEventPublisher(mockKafkaTemplate, topic);

        assertThat(publisher, is(notNullValue()));
    }

    @Test
    void testPublishRestaurantCreatedEventSuccessfully() throws ExecutionException, InterruptedException {

        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant(ownerId, "Test Restaurant", "Test Description", "123 Main St", "555-1234", "test@example.com");
        restaurant.setId(restaurantId);
        restaurant.setStatus(RestaurantStatus.ACTIVE);
        SendResult<String, Object> sendResult = mock(SendResult.class);
        org.apache.kafka.clients.producer.RecordMetadata recordMetadata = mock(org.apache.kafka.clients.producer.RecordMetadata.class);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        when(recordMetadata.partition()).thenReturn(0);
        when(recordMetadata.offset()).thenReturn(1L);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(restaurantEventsTopic), eq(ownerId.toString()), any(RestaurantCreatedEvent.class))).thenReturn(future);

        restaurantEventPublisher.publishRestaurantCreatedEvent(restaurant);

        verify(kafkaTemplate, atLeast(1)).send(eq(restaurantEventsTopic), eq(ownerId.toString()), any(RestaurantCreatedEvent.class));
    }

    @Test
    void testPublishRestaurantCreatedEventWithKafkaFailure() {

        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant(ownerId, "Test Restaurant", "Test Description", "123 Main St", "555-1234", "test@example.com");
        restaurant.setId(restaurantId);
        restaurant.setStatus(RestaurantStatus.PENDING_APPROVAL);
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka send failed"));
        when(kafkaTemplate.send(eq(restaurantEventsTopic), eq(ownerId.toString()), any(RestaurantCreatedEvent.class))).thenReturn(future);

        restaurantEventPublisher.publishRestaurantCreatedEvent(restaurant);

        verify(kafkaTemplate, atLeast(1)).send(eq(restaurantEventsTopic), eq(ownerId.toString()), any(RestaurantCreatedEvent.class));
    }

    @Test
    void testPublishRestaurantCreatedEventWithNullRestaurant() {

        assertThrows(RuntimeException.class, () -> {
            restaurantEventPublisher.publishRestaurantCreatedEvent(null);
        });
    }

    @Test
    void testPublishRestaurantCreatedEventWithInactiveStatus() {

        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant(ownerId, "Test Restaurant", "Test Description", "123 Main St", "555-1234", "test@example.com");
        restaurant.setId(restaurantId);
        restaurant.setStatus(RestaurantStatus.INACTIVE);
        SendResult<String, Object> sendResult = mock(SendResult.class);
        org.apache.kafka.clients.producer.RecordMetadata recordMetadata = mock(org.apache.kafka.clients.producer.RecordMetadata.class);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        when(recordMetadata.partition()).thenReturn(1);
        when(recordMetadata.offset()).thenReturn(5L);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(restaurantEventsTopic), eq(ownerId.toString()), any(RestaurantCreatedEvent.class))).thenReturn(future);

        restaurantEventPublisher.publishRestaurantCreatedEvent(restaurant);

        verify(kafkaTemplate, atLeast(1)).send(eq(restaurantEventsTopic), eq(ownerId.toString()), any(RestaurantCreatedEvent.class));
    }

    @Test
    void testPublishRestaurantCreatedEventWithKafkaTemplateThrowingException() {

        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant(ownerId, "Test Restaurant", "Test Description", "123 Main St", "555-1234", "test@example.com");
        restaurant.setId(restaurantId);
        restaurant.setStatus(RestaurantStatus.ACTIVE);
        when(kafkaTemplate.send(anyString(), anyString(), any(RestaurantCreatedEvent.class))).thenThrow(new RuntimeException("Kafka template exception"));

        assertThrows(RuntimeException.class, () -> {
            restaurantEventPublisher.publishRestaurantCreatedEvent(restaurant);
        });
    }

    @Test
    void testPublishRestaurantCreatedEventWithDifferentPartitionAndOffset() {

        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant(ownerId, "Another Restaurant", "Another Description", "456 Oak Ave", "555-5678", "another@example.com");
        restaurant.setId(restaurantId);
        restaurant.setStatus(RestaurantStatus.PENDING_APPROVAL);
        SendResult<String, Object> sendResult = mock(SendResult.class);
        org.apache.kafka.clients.producer.RecordMetadata recordMetadata = mock(org.apache.kafka.clients.producer.RecordMetadata.class);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        when(recordMetadata.partition()).thenReturn(3);
        when(recordMetadata.offset()).thenReturn(100L);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(restaurantEventsTopic), eq(ownerId.toString()), any(RestaurantCreatedEvent.class))).thenReturn(future);

        restaurantEventPublisher.publishRestaurantCreatedEvent(restaurant);

        verify(kafkaTemplate, atLeast(1)).send(eq(restaurantEventsTopic), eq(ownerId.toString()), any(RestaurantCreatedEvent.class));
    }
}
