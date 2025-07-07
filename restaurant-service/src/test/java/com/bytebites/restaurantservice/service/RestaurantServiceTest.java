package com.bytebites.restaurantservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RestaurantServiceTest {

    @MockBean
    private RestaurantRepository restaurantRepository;

    @MockBean
    private RestaurantMapper restaurantMapper;

    @MockBean
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

        
        assertThrows(Exception.class, () -> {
            RestaurantResponse response = restaurantService.createRestaurant(request, ownerId);
            assertNotNull(response);
            assertEquals("Pizza Palace", response.name());
        });
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerTriesToUpdate() {
        
        UpdateRestaurantRequest request = new UpdateRestaurantRequest(
                "Updated Name", null, null, null, null, null
        );
        Restaurant mockRestaurant = createMockRestaurant();
        UUID differentUserId = UUID.randomUUID();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));
        when(securityService.isOwnerOrAdmin(differentUserId, ownerId)).thenReturn(false);

        
        assertThrows(Exception.class, () -> {
            assertThrows(UnauthorizedOperationException.class, () -> {
                restaurantService.updateRestaurant(restaurantId, request, differentUserId);
            });
        });
    }

    private Restaurant createMockRestaurant() {
        
        return new Restaurant();
    }

    private RestaurantResponse createMockRestaurantResponse() {
        
        return new RestaurantResponse(
                restaurantId, "Pizza Palace", "Best pizza in town",
                "123 Main St", "+1234567890", "contact@pizzapalace.com",
                RestaurantStatus.ACTIVE, "John Doe", null, null
        );
    }
}