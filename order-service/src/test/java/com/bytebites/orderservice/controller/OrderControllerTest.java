package com.bytebites.orderservice.controller;

import com.bytebites.orderservice.dto.CreateOrderRequest;
import com.bytebites.orderservice.dto.OrderItemRequest;
import com.bytebites.orderservice.dto.OrderResponse;
import com.bytebites.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldCreateOrder() throws Exception {
        
        UUID restaurantId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest(
                restaurantId,
                "123 Main St",
                "Please ring doorbell",
                List.of(new OrderItemRequest(UUID.randomUUID(), 2, "Extra cheese"))
        );

        OrderResponse mockResponse = new OrderResponse(
                UUID.randomUUID(),
                UUID.fromString(USER_ID),
                "Customer",
                restaurantId,
                "Test Restaurant",
                null,
                null,
                "123 Main St",
                "Please ring doorbell",
                new ArrayList<>(),
                null,
                null,
                null
        );

        when(orderService.createOrder(any(CreateOrderRequest.class), any(UUID.class)))
                .thenReturn(mockResponse);

        
        mockMvc.perform(post("/api/orders")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldGetMyOrders() throws Exception {
        
        List<OrderResponse> mockOrders = new ArrayList<>();
        when(orderService.getMyOrders(any(UUID.class)))
                .thenReturn(mockOrders);

        
        mockMvc.perform(get("/api/orders")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk());
    }
}