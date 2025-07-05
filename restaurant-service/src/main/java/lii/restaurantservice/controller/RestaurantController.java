package lii.restaurantservice.controller;

import lii.restaurantservice.dto.CreateMenuItemRequest;
import lii.restaurantservice.dto.CreateRestaurantRequest;
import lii.restaurantservice.model.MenuItem;
import lii.restaurantservice.model.Restaurant;
import lii.restaurantservice.repository.RestaurantRepository;
import lii.restaurantservice.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;

    @GetMapping
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Restaurant> createRestaurant(
            @RequestBody CreateRestaurantRequest request,
            @RequestHeader("X-User-Id") String userId) {

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.name());
        restaurant.setAddress(request.address());
        restaurant.setOwnerId(UUID.fromString(userId));
        Restaurant saved = restaurantRepository.save(restaurant);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{restaurantId}/menu")
    public List<MenuItem> getRestaurantMenu(@PathVariable UUID restaurantId) {
        return restaurantService.getMenu(restaurantId);
    }

    @PostMapping("/{restaurantId}/menu-items")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Void> addMenuItem(
            @PathVariable UUID restaurantId,
            @RequestBody CreateMenuItemRequest request,
            @RequestHeader("X-User-Id") String userId) {

        MenuItem newItem = new MenuItem();
        newItem.setName(request.name());
        newItem.setDescription(request.description());
        newItem.setPrice(request.price());

        restaurantService.addMenuItem(restaurantId, newItem, userId);
        return ResponseEntity.ok().build();
    }
}