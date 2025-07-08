package com.bytebites.orderservice.service;

import com.bytebites.orderservice.dto.*;
import com.bytebites.orderservice.enums.OrderStatus;
import com.bytebites.orderservice.exception.InvalidOrderStateException;
import com.bytebites.orderservice.exception.OrderNotFoundException;
import com.bytebites.orderservice.exception.UnauthorizedOperationException;
import com.bytebites.orderservice.mapper.OrderMapper;
import com.bytebites.orderservice.model.Order;
import com.bytebites.orderservice.model.OrderItem;
import com.bytebites.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderValidationService orderValidationService;
    private final RestaurantServiceClient restaurantServiceClient;
    private final EventPublishingService eventPublishingService;

    public OrderService(OrderRepository orderRepository,
                        OrderMapper orderMapper,
                        OrderValidationService orderValidationService,
                        RestaurantServiceClient restaurantServiceClient,
                        EventPublishingService eventPublishingService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderValidationService = orderValidationService;
        this.restaurantServiceClient = restaurantServiceClient;
        this.eventPublishingService = eventPublishingService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse createOrder(CreateOrderRequest request, UUID customerId) {
        logger.info("Creating order for customer: {} at restaurant: {}", customerId, request.restaurantId());

        
        orderValidationService.validateOrderRequest(request, customerId);

        
        Order order = orderMapper.toEntity(request, customerId);

        
        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem orderItem = orderMapper.toOrderItem(itemRequest, order);
            order.addOrderItem(orderItem);
        }

        
        Order savedOrder = orderRepository.save(order);

        
        eventPublishingService.publishOrderPlacedEvent(savedOrder);

        logger.info("Order created successfully with ID: {}", savedOrder.getId());
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(UUID customerId) {
        logger.info("Fetching orders for customer: {}", customerId);

        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return orderMapper.toResponseList(orders);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrdersPaged(UUID customerId, Pageable pageable) {
        logger.info("Fetching paged orders for customer: {}", customerId);

        Page<Order> orders = orderRepository.findByCustomerId(customerId, pageable);
        return orders.map(orderMapper::toResponseWithoutItems);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId, UUID userId) {
        logger.info("Fetching order: {} for user: {}", orderId, userId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        
        if (!order.getCustomerId().equals(userId)) {
            
            throw new UnauthorizedOperationException("You are not authorized to view this order");
        }

        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(UUID restaurantId, UUID restaurantOwnerId) {
        logger.info("Fetching orders for restaurant: {} by owner: {}", restaurantId, restaurantOwnerId);

        
        RestaurantInfo restaurant = restaurantServiceClient.getRestaurant(restaurantId);
        if (!restaurant.ownerId().equals(restaurantOwnerId)) {
            throw new UnauthorizedOperationException("You are not authorized to view orders for this restaurant");
        }

        List<Order> orders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        return orderMapper.toResponseList(orders);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantPendingOrders(UUID restaurantId, UUID restaurantOwnerId) {
        logger.info("Fetching pending orders for restaurant: {} by owner: {}", restaurantId, restaurantOwnerId);

        
        RestaurantInfo restaurant = restaurantServiceClient.getRestaurant(restaurantId);
        if (!restaurant.ownerId().equals(restaurantOwnerId)) {
            throw new UnauthorizedOperationException("You are not authorized to view orders for this restaurant");
        }

        List<Order> orders = orderRepository.findByRestaurantIdAndStatus(restaurantId, OrderStatus.PENDING);
        return orderMapper.toResponseList(orders);
    }

    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    public OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request, UUID userId) {
        logger.info("Updating order status: {} to {} by user: {}", orderId, request.status(), userId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        
        OrderStatus previousStatus = order.getStatus();
        orderValidationService.canUpdateOrderStatus(order, request.status(), userId);

        
        order.setStatus(request.status());

        
        if (request.status() == OrderStatus.CONFIRMED) {
            order.setConfirmedAt(LocalDateTime.now());
        } else if (request.status() == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);

        
        eventPublishingService.publishOrderStatusChangedEvent(updatedOrder, previousStatus, userId);

        logger.info("Order status updated successfully: {} -> {}", previousStatus, request.status());
        return orderMapper.toResponse(updatedOrder);
    }

    // Method lacks @PreAuthorize annotation
    public void cancelOrder(UUID orderId, UUID userId) {
        logger.info("Cancelling order: {} by user: {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        
        if (!order.getCustomerId().equals(userId)) {
            
            throw new UnauthorizedOperationException("You are not authorized to cancel this order");
        }

        
        if (!order.canBeCancelledByCustomer()) {
            throw new InvalidOrderStateException("Order cannot be cancelled in current state: " + order.getStatus());
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        
        eventPublishingService.publishOrderStatusChangedEvent(order, previousStatus, userId);

        logger.info("Order cancelled successfully: {}", orderId);
    }

    @Transactional(readOnly = true)
    public Long getRestaurantOrderCount(UUID restaurantId, OrderStatus status) {
        logger.info("Getting order count for restaurant: {} with status: {}", restaurantId, status);

        return orderRepository.countByRestaurantIdAndStatus(restaurantId, status);
    }
}