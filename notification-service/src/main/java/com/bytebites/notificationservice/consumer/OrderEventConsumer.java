package com.bytebites.notificationservice.consumer;

import com.bytebites.notificationservice.event.OrderPlacedEvent;
import com.bytebites.notificationservice.event.OrderStatusChangedEvent;
import com.bytebites.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.retry.annotation.Retry;
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
public class OrderEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "${bytebites.kafka.topics.order-events}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderEvent(@Payload Map<String, Object> eventMap,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 Acknowledgment acknowledgment) {

        logger.info("Received event from topic: {}, partition: {}, offset: {}, payload type: {}",
                topic, partition, offset, eventMap.getClass().getSimpleName());

        try {
            String eventType = (String) eventMap.get("eventType");
            logger.info("Processing event of type: {}", eventType);

            switch (eventType) {
                case "OrderPlaced":
                    OrderPlacedEvent orderPlacedEvent = convertToOrderPlacedEvent(eventMap);
                    handleOrderPlacedEventWithRetry(orderPlacedEvent);
                    break;
                case "OrderStatusChanged":
                    OrderStatusChangedEvent statusChangedEvent = convertToOrderStatusChangedEvent(eventMap);
                    handleOrderStatusChangedEventWithRetry(statusChangedEvent);
                    break;
                default:
                    logger.warn("Unknown event type received: {}", eventType);
            }

            acknowledgment.acknowledge();
            logger.info("Event processed successfully");

        } catch (Exception e) {
            logger.error("Error processing event: {}", e.getMessage(), e);
            throw e;
        }
    }

    private OrderPlacedEvent convertToOrderPlacedEvent(Map<String, Object> eventMap) {
        try {
            return objectMapper.convertValue(eventMap, OrderPlacedEvent.class);
        } catch (Exception e) {
            logger.error("Error converting to OrderPlacedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert event to OrderPlacedEvent", e);
        }
    }

    private OrderStatusChangedEvent convertToOrderStatusChangedEvent(Map<String, Object> eventMap) {
        try {
            return objectMapper.convertValue(eventMap, OrderStatusChangedEvent.class);
        } catch (Exception e) {
            logger.error("Error converting to OrderStatusChangedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert event to OrderStatusChangedEvent", e);
        }
    }

    @Retry(name = "order-event-processing", fallbackMethod = "fallbackOrderStatusChangedEvent")
    private void handleOrderStatusChangedEventWithRetry(OrderStatusChangedEvent event) {
        logger.info("Processing OrderStatusChangedEvent with retry: orderId={}, status={}->{}",
                event.orderId(), event.previousStatus(), event.newStatus());

        
        notificationService.sendOrderStatusChangedNotification(event);
    }

    
    public void fallbackOrderPlacedEvent(OrderPlacedEvent event, Exception ex) {
        logger.error("Failed to process OrderPlacedEvent after retries: orderId={}, error: {}",
                event.orderId(), ex.getMessage());

        
        saveFailedEventProcessing("OrderPlacedEvent", event.orderId().toString(), ex.getMessage());
    }

    public void fallbackOrderStatusChangedEvent(OrderStatusChangedEvent event, Exception ex) {
        logger.error("Failed to process OrderStatusChangedEvent after retries: orderId={}, error: {}",
                event.orderId(), ex.getMessage());

        
        saveFailedEventProcessing("OrderStatusChangedEvent", event.orderId().toString(), ex.getMessage());
    }

    private void saveFailedEventProcessing(String eventType, String orderId, String error) {
        logger.warn("Saving failed event processing: type={}, orderId={}, error={}", eventType, orderId, error);
        
    }
    @Retry(name = "order-event-processing", fallbackMethod = "fallbackOrderPlacedEvent")
    private void handleOrderPlacedEventWithRetry(OrderPlacedEvent event) {
        logger.info("Processing OrderPlacedEvent with retry: orderId={}, customerId={}, restaurantId={}",
                event.orderId(), event.customerId(), event.restaurantId());

        
        notificationService.sendOrderPlacedNotificationToCustomer(event);

        
        notificationService.sendOrderPlacedNotificationToRestaurant(event);
    }
}