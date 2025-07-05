package lii.orderservice.controller;

import lii.orderservice.dto.CreateOrderRequest;
import lii.orderservice.dto.OrderResponse;
import lii.orderservice.event.OrderPlacedEvent;
import lii.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody CreateOrderRequest createOrderRequest, @RequestHeader("X-User-Id") UUID userId) {
        OrderResponse orderResponse = orderService.placeOrder(createOrderRequest, userId);
        return ResponseEntity.ok(orderResponse);
    }
}
