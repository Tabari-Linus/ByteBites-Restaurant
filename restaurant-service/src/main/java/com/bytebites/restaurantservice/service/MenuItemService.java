package com.bytebites.restaurantservice.service;

import com.bytebites.restaurantservice.dto.CreateMenuItemRequest;
import com.bytebites.restaurantservice.dto.MenuItemResponse;
import com.bytebites.restaurantservice.dto.UpdateMenuItemRequest;
import com.bytebites.restaurantservice.exception.MenuItemNotFoundException;
import com.bytebites.restaurantservice.exception.RestaurantNotFoundException;
import com.bytebites.restaurantservice.exception.UnauthorizedOperationException;
import com.bytebites.restaurantservice.mapper.MenuItemMapper;
import com.bytebites.restaurantservice.model.MenuItem;
import com.bytebites.restaurantservice.model.Restaurant;
import com.bytebites.restaurantservice.repository.MenuItemRepository;
import com.bytebites.restaurantservice.repository.RestaurantRepository;
import com.bytebites.restaurantservice.security.SecurityService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MenuItemService {

    private static final Logger logger = LoggerFactory.getLogger(MenuItemService.class);

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemMapper menuItemMapper;
    private final SecurityService securityService;

    public MenuItemService(MenuItemRepository menuItemRepository,
                           RestaurantRepository restaurantRepository,
                           MenuItemMapper menuItemMapper,
                           SecurityService securityService) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemMapper = menuItemMapper;
        this.securityService = securityService;
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuByRestaurantId(UUID restaurantId) {
        logger.info("Fetching menu for restaurant: {}", restaurantId);

        
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException("Restaurant not found with ID: " + restaurantId);
        }

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdOrderByCategory(restaurantId);
        return menuItemMapper.toResponseList(menuItems);
    }

    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    public MenuItemResponse addMenuItem(UUID restaurantId, CreateMenuItemRequest request, UUID currentUserId) {
        logger.info("Adding menu item: {} to restaurant: {} by user: {}", request.name(), restaurantId, currentUserId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with ID: " + restaurantId));

        
        if (!securityService.isOwnerOrAdmin(currentUserId, restaurant.getOwnerId())) {
            throw new UnauthorizedOperationException("You are not authorized to add menu items to this restaurant");
        }

        MenuItem menuItem = menuItemMapper.toEntity(request, restaurant);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        logger.info("Menu item added successfully with ID: {}", savedMenuItem.getId());
        return menuItemMapper.toResponse(savedMenuItem);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItem(UUID restaurantId, UUID menuItemId) {
        logger.info("Fetching menu item: {} from restaurant: {}", menuItemId, restaurantId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));

        return menuItemMapper.toResponse(menuItem);
    }

    public MenuItemResponse updateMenuItem(UUID restaurantId, UUID menuItemId, UpdateMenuItemRequest request, UUID currentUserId) {
        logger.info("Updating menu item: {} in restaurant: {} by user: {}", menuItemId, restaurantId, currentUserId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));

        
        if (!securityService.isOwnerOrAdmin(currentUserId, menuItem.getRestaurant().getOwnerId())) {
            throw new UnauthorizedOperationException("You are not authorized to update this menu item");
        }

        menuItemMapper.updateEntityFromRequest(request, menuItem);
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);

        logger.info("Menu item updated successfully: {}", menuItemId);
        return menuItemMapper.toResponse(updatedMenuItem);
    }

    public void deleteMenuItem(UUID restaurantId, UUID menuItemId, UUID currentUserId) {
        logger.info("Deleting menu item: {} from restaurant: {} by user: {}", menuItemId, restaurantId, currentUserId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));

        
        if (!securityService.isOwnerOrAdmin(currentUserId, menuItem.getRestaurant().getOwnerId())) {
            throw new UnauthorizedOperationException("You are not authorized to delete this menu item");
        }

        menuItemRepository.delete(menuItem);
        logger.info("Menu item deleted successfully: {}", menuItemId);
    }
}