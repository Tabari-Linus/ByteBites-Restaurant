package com.bytebites.orderservice.service;

import com.bytebites.orderservice.dto.CreateOrderRequest;
import com.bytebites.orderservice.dto.OrderItemRequest;
import com.bytebites.orderservice.dto.OrderResponse;
import com.bytebites.orderservice.dto.UpdateOrderStatusRequest;
import com.bytebites.orderservice.enums.OrderStatus;
import com.bytebites.orderservice.exception.OrderNotFoundException;
import com.bytebites.orderservice.mapper.OrderMapper;
import com.bytebites.orderservice.model.Order;
import com.bytebites.orderservice.model.OrderItem;
import com.bytebites.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderValidationService orderValidationService;

    @Mock
    private RestaurantServiceClient restaurantServiceClient;

    @Mock
    private EventPublishingService eventPublishingService;

    private OrderService orderService;

    private UUID customerId;
    private UUID restaurantId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        
        customerId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        orderService = new OrderService(orderRepository, orderMapper, orderValidationService,
                restaurantServiceClient, eventPublishingService);
    }



    @Test
    void shouldCreateOrder() {
        
        CreateOrderRequest request = new CreateOrderRequest(
                restaurantId,
                "123 Main St",
                "Please ring doorbell",
                List.of(new OrderItemRequest(UUID.randomUUID(), 2, "Extra cheese"))
        );

        Order mockOrder = createMockOrder();
        OrderResponse mockResponse = createMockOrderResponse();

        
        OrderItem mockOrderItem = new OrderItem();
        mockOrderItem.setQuantity(2);
        mockOrderItem.setUnitPrice(BigDecimal.TEN); 
        mockOrderItem.setSubtotal(BigDecimal.TEN.multiply(BigDecimal.valueOf(2))); 

        when(orderValidationService.validateOrderRequest(request, customerId)).thenReturn(true);
        when(orderMapper.toEntity(request, customerId)).thenReturn(mockOrder);
        when(orderMapper.toOrderItem(any(OrderItemRequest.class), any(Order.class))).thenReturn(mockOrderItem);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(orderMapper.toResponse(mockOrder)).thenReturn(mockResponse);

        
        OrderResponse response = orderService.createOrder(request, customerId);

        
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(orderId, response.id());
        assertEquals(customerId, response.customerId());
    }

    @Test
    void shouldUpdateOrderStatus() {
        
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);
        Order mockOrder = createMockOrder();
        UUID restaurantOwnerId = UUID.randomUUID();
        OrderResponse mockResponse = createMockOrderResponse();

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderValidationService.canUpdateOrderStatus(mockOrder, request.status(), restaurantOwnerId)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(orderMapper.toResponse(mockOrder)).thenReturn(mockResponse);

        
        OrderResponse response = orderService.updateOrderStatus(orderId, request, restaurantOwnerId);

        
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(orderId, response.id());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        
        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderById(orderId, customerId);
        });

        
        assertEquals("Order not found: " + orderId, exception.getMessage());
    }

    private Order createMockOrder() {
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setRestaurantId(restaurantId);
        order.setRestaurantName("Test Restaurant");
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress("123 Main St");
        order.setTotalAmount(BigDecimal.ZERO);
        order.setOrderItems(new ArrayList<>());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    private OrderResponse createMockOrderResponse() {
        
        return new OrderResponse(
                orderId, customerId, "John Doe", restaurantId, "Pizza Palace",
                OrderStatus.PENDING, BigDecimal.valueOf(25.99), "123 Main St",
                "Please ring doorbell", List.of(), null, null, null
        );
    }
}