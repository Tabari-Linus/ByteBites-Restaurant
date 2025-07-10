package com.bytebites.orderservice.controller;

import com.bytebites.orderservice.dto.CreateOrderRequest;
import com.bytebites.orderservice.dto.OrderResponse;
import com.bytebites.orderservice.dto.UpdateOrderStatusRequest;
import com.bytebites.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Create order request from customer: {}", userId);
        OrderResponse response = orderService.createOrder(request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(@RequestHeader("X-User-Id") String userId) {
        logger.info("Get my orders request from user: {}", userId);

        List<OrderResponse> orders = orderService.getMyOrders(UUID.fromString(userId));
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<OrderResponse>> getMyOrdersPaged(
            @RequestHeader("X-User-Id") String userId,
            Pageable pageable) {
        logger.info("Get my orders (paged) request from user: {}", userId);
        Page<OrderResponse> orders = orderService.getMyOrdersPaged(UUID.fromString(userId), pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Get order by ID request: {} from user: {}", id, userId);
        OrderResponse order = orderService.getOrderById(id, UUID.fromString(userId));
        return ResponseEntity.ok(order);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponse>> getRestaurantOrders(
            @PathVariable UUID restaurantId,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Get restaurant orders request for restaurant: {} from user: {}", restaurantId, userId);
        List<OrderResponse> orders = orderService.getRestaurantOrders(restaurantId, UUID.fromString(userId));
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/restaurant/{restaurantId}/pending")
    public ResponseEntity<List<OrderResponse>> getRestaurantPendingOrders(
            @PathVariable UUID restaurantId,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Get restaurant pending orders request for restaurant: {} from user: {}", restaurantId, userId);
        List<OrderResponse> orders = orderService.getRestaurantPendingOrders(restaurantId, UUID.fromString(userId));
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Update order status request: {} to {} from user: {}", id, request.status(), userId);
        OrderResponse response = orderService.updateOrderStatus(id, request, UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Cancel order request: {} from user: {}", id, userId);
        orderService.cancelOrder(id, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}