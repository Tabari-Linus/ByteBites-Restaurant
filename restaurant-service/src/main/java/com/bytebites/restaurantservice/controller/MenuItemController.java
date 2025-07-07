package com.bytebites.restaurantservice.controller;

import com.bytebites.restaurantservice.dto.CreateMenuItemRequest;
import com.bytebites.restaurantservice.dto.MenuItemResponse;
import com.bytebites.restaurantservice.dto.UpdateMenuItemRequest;
import com.bytebites.restaurantservice.service.MenuItemService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MenuItemController {

    private static final Logger logger = LoggerFactory.getLogger(MenuItemController.class);

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getMenu(@PathVariable UUID restaurantId) {
        logger.info("Get menu request for restaurant: {}", restaurantId);

        List<MenuItemResponse> menu = menuItemService.getMenuByRestaurantId(restaurantId);
        return ResponseEntity.ok(menu);
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CreateMenuItemRequest request,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Add menu item request for restaurant: {} from user: {}", restaurantId, userId);

        MenuItemResponse response = menuItemService.addMenuItem(restaurantId, request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<MenuItemResponse> getMenuItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId) {
        logger.info("Get menu item request: {} from restaurant: {}", itemId, restaurantId);

        MenuItemResponse menuItem = menuItemService.getMenuItem(restaurantId, itemId);
        return ResponseEntity.ok(menuItem);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateMenuItemRequest request,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Update menu item request: {} in restaurant: {} from user: {}", itemId, restaurantId, userId);

        MenuItemResponse response = menuItemService.updateMenuItem(restaurantId, itemId, request, UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @RequestHeader("X-User-Id") String userId) {
        logger.info("Delete menu item request: {} from restaurant: {} by user: {}", itemId, restaurantId, userId);

        menuItemService.deleteMenuItem(restaurantId, itemId, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}