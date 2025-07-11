package com.bytebites.restaurantservice.integration;

import com.bytebites.restaurantservice.enums.RestaurantStatus;
import com.bytebites.restaurantservice.model.MenuItem;
import com.bytebites.restaurantservice.model.Restaurant;
import com.bytebites.restaurantservice.repository.MenuItemRepository;
import com.bytebites.restaurantservice.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RestaurantRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    private UUID ownerId;
    private Restaurant testRestaurant;

    @BeforeEach
    void setUpEach() {
        ownerId = UUID.randomUUID();

        testRestaurant = new Restaurant();
        testRestaurant.setOwnerId(ownerId);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setDescription("Test Description");
        testRestaurant.setAddress("123 Test St");
        testRestaurant.setPhone("+1234567890");
        testRestaurant.setEmail("test@restaurant.com");
        testRestaurant.setStatus(RestaurantStatus.ACTIVE);
    }

    @Test
    void shouldSaveAndRetrieveRestaurant() {
        
        Restaurant saved = restaurantRepository.save(testRestaurant);

        
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("Test Restaurant", saved.getName());
        assertEquals(ownerId, saved.getOwnerId());
    }

    @Test
    void shouldFindRestaurantByOwnerId() {
        
        restaurantRepository.save(testRestaurant);

        
        List<Restaurant> restaurants = restaurantRepository.findByOwnerId(ownerId);

        
        assertEquals(1, restaurants.size());
        assertEquals("Test Restaurant", restaurants.get(0).getName());
    }

    @Test
    void shouldFindActiveRestaurants() {
        
        restaurantRepository.save(testRestaurant);

        Restaurant inactiveRestaurant = new Restaurant();
        inactiveRestaurant.setOwnerId(UUID.randomUUID());
        inactiveRestaurant.setName("Inactive Restaurant");
        inactiveRestaurant.setDescription("Inactive");
        inactiveRestaurant.setAddress("456 Inactive St");
        inactiveRestaurant.setPhone("+9876543210");
        inactiveRestaurant.setStatus(RestaurantStatus.INACTIVE);
        restaurantRepository.save(inactiveRestaurant);

        
        List<Restaurant> activeRestaurants = restaurantRepository.findActiveRestaurantsOrderByCreatedAt(RestaurantStatus.ACTIVE);

        
        assertEquals(1, activeRestaurants.size());
        assertEquals("Test Restaurant", activeRestaurants.get(0).getName());
    }

    @Test
    void shouldPreventDuplicateRestaurantNames() {
        
        restaurantRepository.save(testRestaurant);

        Restaurant duplicate = new Restaurant();
        duplicate.setOwnerId(ownerId);
        duplicate.setName("Test Restaurant");
        duplicate.setDescription("Duplicate");
        duplicate.setAddress("789 Duplicate St");
        duplicate.setPhone("+1111111111");

        
        boolean exists = restaurantRepository.existsByOwnerIdAndName(ownerId, "Test Restaurant");

        
        assertTrue(exists);
    }

    @Test
    void shouldLoadRestaurantWithMenuItems() {
        
        Restaurant saved = restaurantRepository.save(testRestaurant);

        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurant(saved);
        menuItem.setName("Test Pizza");
        menuItem.setDescription("Delicious pizza");
        menuItem.setPrice(BigDecimal.valueOf(12.99));
        menuItem.setCategory("MAIN_COURSE");
        menuItem.setAvailable(true);
        menuItemRepository.save(menuItem);

        
        Optional<Restaurant> result = restaurantRepository.findByIdWithMenuItems(saved.getId());

        
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getMenuItems().size());
        assertEquals("Test Pizza", result.get().getMenuItems().get(0).getName());
    }

}