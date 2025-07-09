package com.bytebites.orderservice.event;

import com.bytebites.orderservice.enums.OrderStatus;
import com.bytebites.orderservice.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private CompletableFuture<SendResult<String, Object>> mockFuture;

    private OrderEventPublisher orderEventPublisher;

    @BeforeEach
    void setUp() {
        orderEventPublisher = new OrderEventPublisher(kafkaTemplate, "order-events");

        
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(mockFuture);
    }

    @Test
    void shouldPublishOrderPlacedEvent() {
        Order order = createMockOrder();

        
        assertDoesNotThrow(() -> {
            orderEventPublisher.publishOrderPlacedEvent(order);
        });

        verify(kafkaTemplate).send(eq("order-events"), anyString(), any(OrderPlacedEvent.class));
    }

    @Test
    void shouldPublishOrderStatusChangedEvent() {
        Order order = createMockOrder();
        OrderStatus previousStatus = OrderStatus.PENDING;
        UUID changedBy = UUID.randomUUID();

        
        assertDoesNotThrow(() -> {
            orderEventPublisher.publishOrderStatusChangedEvent(order, previousStatus, changedBy);
        });

        verify(kafkaTemplate).send(eq("order-events"), anyString(), any(OrderStatusChangedEvent.class));
    }

    private Order createMockOrder() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerId(UUID.randomUUID());
        order.setRestaurantId(UUID.randomUUID());
        order.setRestaurantName("Test Restaurant");
        order.setTotalAmount(BigDecimal.valueOf(25.99));
        order.setDeliveryAddress("123 Test St");
        order.setStatus(OrderStatus.PENDING); 
        return order;
    }
}