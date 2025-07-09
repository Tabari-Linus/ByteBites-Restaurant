package com.bytebites.notificationservice.consumer;

import com.bytebites.notificationservice.event.RestaurantCreatedEvent;
import com.bytebites.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class RestaurantEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantEventConsumer.class);

    private final NotificationService notificationService;

    public RestaurantEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${bytebites.kafka.topics.restaurant-events}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleRestaurantEvent(@Payload Object event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                      @Header(KafkaHeaders.OFFSET) long offset,
                                      Acknowledgment acknowledgment) {

        logger.info("Received event from topic: {}, partition: {}, offset: {}, type: {}",
                topic, partition, offset, event.getClass().getSimpleName());

        try {
            if (event instanceof RestaurantCreatedEvent restaurantCreatedEvent) {
                handleRestaurantCreatedEvent(restaurantCreatedEvent);
            } else {
                logger.warn("Unknown event type received: {}", event.getClass().getSimpleName());
            }

            
            acknowledgment.acknowledge();
            logger.info("Event processed successfully: {}", event.getClass().getSimpleName());

        } catch (Exception e) {
            logger.error("Error processing event: {}, error: {}", event.getClass().getSimpleName(), e.getMessage(), e);
            
            throw e;
        }
    }

    private void handleRestaurantCreatedEvent(RestaurantCreatedEvent event) {
        logger.info("Processing RestaurantCreatedEvent: restaurantId={}, ownerId={}",
                event.restaurantId(), event.ownerId());

        
        notificationService.sendRestaurantCreatedNotification(event);
    }
}