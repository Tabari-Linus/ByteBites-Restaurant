package com.bytebites.notificationservice.consumer;

import com.bytebites.notificationservice.event.RestaurantCreatedEvent;
import com.bytebites.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RestaurantEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public RestaurantEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "${bytebites.kafka.topics.restaurant-events}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleRestaurantEvent(@Payload Object event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                      @Header(KafkaHeaders.OFFSET) long offset,
                                      Acknowledgment acknowledgment) {

        logger.info("Received event from topic: {}, partition: {}, offset: {}, payload type: {}",
                topic, partition, offset, event.getClass().getSimpleName());

        try {
            Map<String, Object> eventMap = null;

            
            if (event instanceof Map) {
                eventMap = (Map<String, Object>) event;
            } else if (event instanceof ConsumerRecord) {
                
                ConsumerRecord<String, Object> record = (ConsumerRecord<String, Object>) event;
                Object payload = record.value();

                if (payload instanceof Map) {
                    eventMap = (Map<String, Object>) payload;
                } else {
                    logger.warn("ConsumerRecord payload is not a Map: {}", payload.getClass().getSimpleName());
                    return;
                }
            } else if (event instanceof RestaurantCreatedEvent restaurantCreatedEvent) {
                handleRestaurantCreatedEvent(restaurantCreatedEvent);
                acknowledgment.acknowledge();
                logger.info("Event processed successfully");
                return;
            } else {
                logger.warn("Unexpected event format: {}", event.getClass().getSimpleName());
                acknowledgment.acknowledge();
                return;
            }

            
            if (eventMap != null) {
                String eventType = (String) eventMap.get("eventType");
                logger.info("Processing event of type: {}", eventType);

                switch (eventType) {
                    case "RestaurantCreated":
                        RestaurantCreatedEvent restaurantCreatedEvent = convertToRestaurantCreatedEvent(eventMap);
                        handleRestaurantCreatedEvent(restaurantCreatedEvent);
                        break;
                    default:
                        logger.warn("Unknown event type received: {}", eventType);
                }
            }

            acknowledgment.acknowledge();
            logger.info("Event processed successfully");

        } catch (Exception e) {
            logger.error("Error processing event: {}", e.getMessage(), e);
            throw e;
        }
    }

    private RestaurantCreatedEvent convertToRestaurantCreatedEvent(Map<String, Object> eventMap) {
        try {
            return objectMapper.convertValue(eventMap, RestaurantCreatedEvent.class);
        } catch (Exception e) {
            logger.error("Error converting to RestaurantCreatedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert event to RestaurantCreatedEvent", e);
        }
    }

    private void handleRestaurantCreatedEvent(RestaurantCreatedEvent event) {
        logger.info("Processing RestaurantCreatedEvent: restaurantId={}, ownerId={}",
                event.restaurantId(), event.ownerId());

        notificationService.sendRestaurantCreatedNotification(event);
    }
}