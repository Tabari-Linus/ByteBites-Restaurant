package com.bytebites.restaurantservice.controller;

import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.service.RestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RestaurantService restaurantService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        RestaurantController restaurantController = new RestaurantController(restaurantService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(restaurantController)
                .build();
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void shouldCreateRestaurant() throws Exception {
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Pizza Palace",
                "Best pizza in town",
                "123 Main St",
                "+1234567890",
                "contact@pizzapalace.com"
        );

        mockMvc.perform(post("/api/restaurants")
                        .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetAllRestaurants() throws Exception {
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk());
    }
}