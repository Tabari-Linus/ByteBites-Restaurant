package com.bytebites.notificationservice.consumer;

import com.bytebites.notificationservice.event.OrderPlacedEvent;
import com.bytebites.notificationservice.event.OrderStatusChangedEvent;
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
public class OrderEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;

    public OrderEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${bytebites.kafka.topics.order-events}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderEvent(@Payload Object event,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 Acknowledgment acknowledgment) {

        logger.info("Received event from topic: {}, partition: {}, offset: {}, type: {}",
                topic, partition, offset, event.getClass().getSimpleName());

        try {
            if (event instanceof OrderPlacedEvent orderPlacedEvent) {
                handleOrderPlacedEvent(orderPlacedEvent);
            } else if (event instanceof OrderStatusChangedEvent statusChangedEvent) {
                handleOrderStatusChangedEvent(statusChangedEvent);
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

    private void handleOrderPlacedEvent(OrderPlacedEvent event) {
        logger.info("Processing OrderPlacedEvent: orderId={}, customerId={}, restaurantId={}",
                event.orderId(), event.customerId(), event.restaurantId());

        
        notificationService.sendOrderPlacedNotificationToCustomer(event);

        
        notificationService.sendOrderPlacedNotificationToRestaurant(event);
    }

    private void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        logger.info("Processing OrderStatusChangedEvent: orderId={}, status={}->{}",
                event.orderId(), event.previousStatus(), event.newStatus());

        
        notificationService.sendOrderStatusChangedNotification(event);
    }
}