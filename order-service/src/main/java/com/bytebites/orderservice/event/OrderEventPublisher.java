package com.bytebites.orderservice.event;

import com.bytebites.orderservice.enums.OrderStatus;
import com.bytebites.orderservice.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderEventsTopic;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                               @Value("${bytebites.kafka.topics.order-events}") String orderEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderEventsTopic = orderEventsTopic;
    }

    public void publishOrderPlacedEvent(Order order) {
        logger.info("Publishing OrderPlacedEvent for order: {}", order.getId());

        try {
            List<OrderItemInfo> orderItems = order.getOrderItems().stream()
                    .map(item -> new OrderItemInfo(
                            item.getMenuItemId(),
                            item.getMenuItemName(),
                            item.getUnitPrice(),
                            item.getQuantity(),
                            item.getSubtotal()
                    ))
                    .toList();

            OrderPlacedEvent event = OrderPlacedEvent.create(
                    order.getId(),
                    order.getCustomerId(),
                    "customer@example.com", 
                    order.getRestaurantId(),
                    order.getRestaurantName(),
                    order.getTotalAmount(),
                    orderItems,
                    order.getDeliveryAddress()
            );

            
            String partitionKey = order.getRestaurantId().toString();

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(orderEventsTopic, partitionKey, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("OrderPlacedEvent sent successfully: eventId={}, partition={}, offset={}",
                            event.eventId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to send OrderPlacedEvent: eventId={}, error: {}",
                            event.eventId(), ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error publishing OrderPlacedEvent for order: {}, error: {}",
                    order.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish OrderPlacedEvent", e);
        }
    }

    public void publishOrderStatusChangedEvent(Order order, OrderStatus previousStatus, UUID changedBy) {
        logger.info("Publishing OrderStatusChangedEvent for order: {} from {} to {}",
                order.getId(), previousStatus, order.getStatus());

        try {
            OrderStatusChangedEvent event = OrderStatusChangedEvent.create(
                    order.getId(),
                    order.getCustomerId(),
                    "customer@example.com", 
                    order.getRestaurantId(),
                    order.getRestaurantName(),
                    previousStatus,
                    order.getStatus(),
                    changedBy
            );

            
            String partitionKey = order.getRestaurantId().toString();

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(orderEventsTopic, partitionKey, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("OrderStatusChangedEvent sent successfully: eventId={}, partition={}, offset={}",
                            event.eventId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to send OrderStatusChangedEvent: eventId={}, error: {}",
                            event.eventId(), ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error publishing OrderStatusChangedEvent for order: {}, error: {}",
                    order.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish OrderStatusChangedEvent", e);
        }
    }
}
