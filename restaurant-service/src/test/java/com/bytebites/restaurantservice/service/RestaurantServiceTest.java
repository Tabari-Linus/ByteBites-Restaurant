package com.bytebites.restaurantservice.service;

import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.dto.RestaurantResponse;
import com.bytebites.restaurantservice.dto.UpdateRestaurantRequest;
import com.bytebites.restaurantservice.enums.RestaurantStatus;
import com.bytebites.restaurantservice.exception.UnauthorizedOperationException;
import com.bytebites.restaurantservice.mapper.RestaurantMapper;
import com.bytebites.restaurantservice.model.Restaurant;
import com.bytebites.restaurantservice.repository.RestaurantRepository;
import com.bytebites.restaurantservice.security.SecurityService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantMapper restaurantMapper;

    @Mock
    private SecurityService securityService;

    private RestaurantService restaurantService;

    private UUID ownerId;
    private UUID restaurantId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        restaurantService = new RestaurantService(restaurantRepository, restaurantMapper, securityService);
    }

    @Test
    void shouldCreateRestaurant() {
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Pizza Palace",
                "Best pizza in town",
                "123 Main St",
                "+1234567890",
                "contact@pizzapalace.com"
        );

        Restaurant mockRestaurant = createMockRestaurant();
        RestaurantResponse mockResponse = createMockRestaurantResponse();

        when(restaurantMapper.toEntity(request, ownerId)).thenReturn(mockRestaurant);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(mockRestaurant);
        when(restaurantMapper.toResponse(mockRestaurant)).thenReturn(mockResponse);

        RestaurantResponse response = restaurantService.createRestaurant(request, ownerId);
        assertNotNull(response);
        assertEquals("Pizza Palace", response.name());
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerTriesToUpdate() {
        UpdateRestaurantRequest request = new UpdateRestaurantRequest(
                "Updated Name", null, null, null, null, null
        );
        Restaurant mockRestaurant = createMockRestaurant();
        mockRestaurant.setOwnerId(ownerId); // Set the owner ID on the mock restaurant
        UUID differentUserId = UUID.randomUUID();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));
        when(securityService.isOwnerOrAdmin(differentUserId, ownerId)).thenReturn(false);

        assertThrows(UnauthorizedOperationException.class, () ->
                restaurantService.updateRestaurant(restaurantId, request, differentUserId)
        );
    }



    private Restaurant createMockRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Pizza Palace");
        restaurant.setDescription("Best pizza in town");
        restaurant.setAddress("123 Main St");
        restaurant.setPhone("+1234567890");
        restaurant.setEmail("contact@pizzapalace.com");
        restaurant.setStatus(RestaurantStatus.ACTIVE);
        restaurant.setOwnerId(ownerId);
        return restaurant;
    }

    private RestaurantResponse createMockRestaurantResponse() {
        return new RestaurantResponse(
                restaurantId, "Pizza Palace", "Best pizza in town",
                "123 Main St", "+1234567890", "contact@pizzapalace.com",
                RestaurantStatus.ACTIVE, "John Doe", null, null
        );
    }
}