package com.bytebites.restaurantservice.event;

import com.bytebites.restaurantservice.model.Restaurant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class RestaurantEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String restaurantEventsTopic;

    public RestaurantEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                    @Value("${bytebites.kafka.topics.restaurant-events}") String restaurantEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.restaurantEventsTopic = restaurantEventsTopic;
    }

    public void publishRestaurantCreatedEvent(Restaurant restaurant) {
        logger.info("Publishing RestaurantCreatedEvent for restaurant: {}", restaurant.getId());
        
        try {
            RestaurantCreatedEvent event = RestaurantCreatedEvent.create(
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getOwnerId(),
                    restaurant.getEmail(),
                    restaurant.getAddress(),
                    restaurant.getStatus().toString()
            );

            
            String partitionKey = restaurant.getOwnerId().toString();

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(restaurantEventsTopic, partitionKey, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("RestaurantCreatedEvent sent successfully: eventId={}, partition={}, offset={}",
                            event.eventId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to send RestaurantCreatedEvent: eventId={}, error: {}",
                            event.eventId(), ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error publishing RestaurantCreatedEvent for restaurant: {}, error: {}",
                    restaurant.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish RestaurantCreatedEvent", e);
        }
    }
}
