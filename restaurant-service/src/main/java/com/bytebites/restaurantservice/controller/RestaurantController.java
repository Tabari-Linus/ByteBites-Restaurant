package com.bytebites.restaurantservice.controller;

import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.dto.RestaurantResponse;
import com.bytebites.restaurantservice.dto.UpdateRestaurantRequest;

import com.bytebites.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RestaurantController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Create restaurant request from user: {}", userId);

        RestaurantResponse response = restaurantService.createRestaurant(request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        logger.info("Get all active restaurants request");

        List<RestaurantResponse> restaurants = restaurantService.getAllActiveRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<RestaurantResponse>> getAllRestaurantsPaged(Pageable pageable) {
        logger.info("Get all restaurants with pagination request");

        Page<RestaurantResponse> restaurants = restaurantService.getAllRestaurants(pageable);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable UUID id) {
        logger.info("Get restaurant by ID request: {}", id);

        RestaurantResponse restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/my")
    public ResponseEntity<List<RestaurantResponse>> getMyRestaurants(@RequestHeader("X-User-Id") String userId) {
        logger.info("Get my restaurants request from user: {}", userId);

        List<RestaurantResponse> restaurants = restaurantService.getMyRestaurants(UUID.fromString(userId));
        return ResponseEntity.ok(restaurants);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRestaurantRequest request,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Update restaurant request: {} from user: {}", id, userId);

        RestaurantResponse response = restaurantService.updateRestaurant(id, request, UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestaurantResponse> updateRestaurantStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRestaurantRequest request,
            @RequestHeader("X-User-Id") String userId) {


        RestaurantResponse response = restaurantService.updateRestaurantStatus(id, request, UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Delete restaurant request: {} from user: {}", id, userId);

        restaurantService.deleteRestaurant(id, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}