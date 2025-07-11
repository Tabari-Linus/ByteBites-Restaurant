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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeast;

@Timeout(10)
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuItemMapper menuItemMapper;

    @Mock
    private SecurityService securityService;

    private MenuItemService menuItemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        menuItemService = new MenuItemService(menuItemRepository, restaurantRepository, menuItemMapper, securityService);
    }

    @Test
    void testGetMenuByRestaurantIdSuccessfully() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        MenuItem menuItem1 = new MenuItem();
        menuItem1.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"));
        menuItem1.setName("Burger");
        menuItem1.setDescription("Delicious burger");
        menuItem1.setPrice(new BigDecimal("10.99"));
        menuItem1.setCategory("Main Course");
        menuItem1.setAvailable(true);
        menuItem1.setCreatedAt(LocalDateTime.now());
        MenuItem menuItem2 = new MenuItem();
        menuItem2.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"));
        menuItem2.setName("Pizza");
        menuItem2.setDescription("Tasty pizza");
        menuItem2.setPrice(new BigDecimal("15.99"));
        menuItem2.setCategory("Main Course");
        menuItem2.setAvailable(true);
        menuItem2.setCreatedAt(LocalDateTime.now());
        List<MenuItem> menuItems = Arrays.asList(menuItem1, menuItem2);
        MenuItemResponse response1 = new MenuItemResponse(menuItem1.getId(), menuItem1.getName(), menuItem1.getDescription(), menuItem1.getPrice(), menuItem1.getCategory(), menuItem1.getAvailable(), menuItem1.getImageUrl(), menuItem1.getCreatedAt());
        MenuItemResponse response2 = new MenuItemResponse(menuItem2.getId(), menuItem2.getName(), menuItem2.getDescription(), menuItem2.getPrice(), menuItem2.getCategory(), menuItem2.getAvailable(), menuItem2.getImageUrl(), menuItem2.getCreatedAt());
        List<MenuItemResponse> expectedResponses = Arrays.asList(response1, response2);
        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);
        when(menuItemRepository.findByRestaurantIdOrderByCategory(restaurantId)).thenReturn(menuItems);
        when(menuItemMapper.toResponseList(menuItems)).thenReturn(expectedResponses);
        List<MenuItemResponse> result = menuItemService.getMenuByRestaurantId(restaurantId);
        assertThat(result, equalTo(expectedResponses));
        verify(restaurantRepository, atLeast(1)).existsById(restaurantId);
        verify(menuItemRepository, atLeast(1)).findByRestaurantIdOrderByCategory(restaurantId);
        verify(menuItemMapper, atLeast(1)).toResponseList(menuItems);
    }

    @Test
    void testGetMenuByRestaurantIdWithEmptyMenu() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        List<MenuItem> emptyMenuItems = Collections.emptyList();
        List<MenuItemResponse> emptyResponses = Collections.emptyList();
        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);
        when(menuItemRepository.findByRestaurantIdOrderByCategory(restaurantId)).thenReturn(emptyMenuItems);
        when(menuItemMapper.toResponseList(emptyMenuItems)).thenReturn(emptyResponses);
        List<MenuItemResponse> result = menuItemService.getMenuByRestaurantId(restaurantId);
        assertThat(result, equalTo(emptyResponses));
        verify(restaurantRepository, atLeast(1)).existsById(restaurantId);
        verify(menuItemRepository, atLeast(1)).findByRestaurantIdOrderByCategory(restaurantId);
        verify(menuItemMapper, atLeast(1)).toResponseList(emptyMenuItems);
    }

    @Test
    void testGetMenuByRestaurantIdThrowsRestaurantNotFoundException() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(restaurantRepository.existsById(restaurantId)).thenReturn(false);
        RestaurantNotFoundException exception = assertThrows(RestaurantNotFoundException.class, () -> menuItemService.getMenuByRestaurantId(restaurantId));
        assertThat(exception.getMessage(), equalTo("Restaurant not found with ID: " + restaurantId));
        verify(restaurantRepository, atLeast(1)).existsById(restaurantId);
    }

    @Test
    void testAddMenuItemSuccessfully() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID currentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        UUID ownerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        CreateMenuItemRequest request = new CreateMenuItemRequest("Burger", "Delicious burger", new BigDecimal("10.99"), "Main Course", "http://example.com/burger.jpg");
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setOwnerId(ownerId);
        restaurant.setName("Test Restaurant");
        MenuItem menuItem = new MenuItem();
        menuItem.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"));
        menuItem.setName(request.name());
        menuItem.setDescription(request.description());
        menuItem.setPrice(request.price());
        menuItem.setCategory(request.category());
        menuItem.setRestaurant(restaurant);
        menuItem.setAvailable(true);
        menuItem.setCreatedAt(LocalDateTime.now());
        MenuItem savedMenuItem = new MenuItem();
        savedMenuItem.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"));
        savedMenuItem.setName(request.name());
        savedMenuItem.setDescription(request.description());
        savedMenuItem.setPrice(request.price());
        savedMenuItem.setCategory(request.category());
        savedMenuItem.setRestaurant(restaurant);
        savedMenuItem.setAvailable(true);
        savedMenuItem.setCreatedAt(LocalDateTime.now());
        MenuItemResponse expectedResponse = new MenuItemResponse(savedMenuItem.getId(), savedMenuItem.getName(), savedMenuItem.getDescription(), savedMenuItem.getPrice(), savedMenuItem.getCategory(), savedMenuItem.getAvailable(), savedMenuItem.getImageUrl(), savedMenuItem.getCreatedAt());
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(securityService.isOwnerOrAdmin(currentUserId, ownerId)).thenReturn(true);
        when(menuItemMapper.toEntity(request, restaurant)).thenReturn(menuItem);
        when(menuItemRepository.save(menuItem)).thenReturn(savedMenuItem);
        when(menuItemMapper.toResponse(savedMenuItem)).thenReturn(expectedResponse);
        MenuItemResponse result = menuItemService.addMenuItem(restaurantId, request, currentUserId);
        assertThat(result, equalTo(expectedResponse));
        verify(restaurantRepository, atLeast(1)).findById(restaurantId);
        verify(securityService, atLeast(1)).isOwnerOrAdmin(currentUserId, ownerId);
        verify(menuItemMapper, atLeast(1)).toEntity(request, restaurant);
        verify(menuItemRepository, atLeast(1)).save(menuItem);
        verify(menuItemMapper, atLeast(1)).toResponse(savedMenuItem);
    }

    @Test
    void testAddMenuItemThrowsRestaurantNotFoundException() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID currentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        CreateMenuItemRequest request = new CreateMenuItemRequest("Burger", "Delicious burger", new BigDecimal("10.99"), "Main Course", "http://example.com/burger.jpg");
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());
        RestaurantNotFoundException exception = assertThrows(RestaurantNotFoundException.class, () -> menuItemService.addMenuItem(restaurantId, request, currentUserId));
        assertThat(exception.getMessage(), equalTo("Restaurant not found with ID: " + restaurantId));
        verify(restaurantRepository, atLeast(1)).findById(restaurantId);
    }

    @Test
    void testAddMenuItemThrowsUnauthorizedOperationException() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID currentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        UUID ownerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        CreateMenuItemRequest request = new CreateMenuItemRequest("Burger", "Delicious burger", new BigDecimal("10.99"), "Main Course", "http://example.com/burger.jpg");
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setOwnerId(ownerId);
        restaurant.setName("Test Restaurant");
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(securityService.isOwnerOrAdmin(currentUserId, ownerId)).thenReturn(false);
        UnauthorizedOperationException exception = assertThrows(UnauthorizedOperationException.class, () -> menuItemService.addMenuItem(restaurantId, request, currentUserId));
        assertThat(exception.getMessage(), equalTo("You are not authorized to add menu items to this restaurant"));
        verify(restaurantRepository, atLeast(1)).findById(restaurantId);
        verify(securityService, atLeast(1)).isOwnerOrAdmin(currentUserId, ownerId);
    }

    @Test
    void testGetMenuItemSuccessfully() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID menuItemId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        MenuItem menuItem = new MenuItem();
        menuItem.setId(menuItemId);
        menuItem.setName("Burger");
        menuItem.setDescription("Delicious burger");
        menuItem.setPrice(new BigDecimal("10.99"));
        menuItem.setCategory("Main Course");
        menuItem.setAvailable(true);
        menuItem.setCreatedAt(LocalDateTime.now());
        MenuItemResponse expectedResponse = new MenuItemResponse(menuItem.getId(), menuItem.getName(), menuItem.getDescription(), menuItem.getPrice(), menuItem.getCategory(), menuItem.getAvailable(), menuItem.getImageUrl(), menuItem.getCreatedAt());
        when(menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)).thenReturn(Optional.of(menuItem));
        when(menuItemMapper.toResponse(menuItem)).thenReturn(expectedResponse);
        MenuItemResponse result = menuItemService.getMenuItem(restaurantId, menuItemId);
        assertThat(result, equalTo(expectedResponse));
        verify(menuItemRepository, atLeast(1)).findByIdAndRestaurantId(menuItemId, restaurantId);
        verify(menuItemMapper, atLeast(1)).toResponse(menuItem);
    }

    @Test
    void testGetMenuItemThrowsMenuItemNotFoundException() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID menuItemId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        when(menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)).thenReturn(Optional.empty());
        MenuItemNotFoundException exception = assertThrows(MenuItemNotFoundException.class, () -> menuItemService.getMenuItem(restaurantId, menuItemId));
        assertThat(exception.getMessage(), equalTo("Menu item not found"));
        verify(menuItemRepository, atLeast(1)).findByIdAndRestaurantId(menuItemId, restaurantId);
    }

    @Test
    void testUpdateMenuItemSuccessfully() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID menuItemId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        UUID currentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        UUID ownerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        UpdateMenuItemRequest request = new UpdateMenuItemRequest("Updated Burger", "Updated description", new BigDecimal("12.99"), "Updated Category", true, "http://example.com/updated-burger.jpg");
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setOwnerId(ownerId);
        restaurant.setName("Test Restaurant");
        MenuItem menuItem = new MenuItem();
        menuItem.setId(menuItemId);
        menuItem.setName("Burger");
        menuItem.setDescription("Delicious burger");
        menuItem.setPrice(new BigDecimal("10.99"));
        menuItem.setCategory("Main Course");
        menuItem.setAvailable(true);
        menuItem.setRestaurant(restaurant);
        menuItem.setCreatedAt(LocalDateTime.now());
        MenuItem updatedMenuItem = new MenuItem();
        updatedMenuItem.setId(menuItemId);
        updatedMenuItem.setName(request.name());
        updatedMenuItem.setDescription(request.description());
        updatedMenuItem.setPrice(request.price());
        updatedMenuItem.setCategory(request.category());
        updatedMenuItem.setAvailable(request.available());
        updatedMenuItem.setRestaurant(restaurant);
        updatedMenuItem.setCreatedAt(LocalDateTime.now());
        MenuItemResponse expectedResponse = new MenuItemResponse(updatedMenuItem.getId(), updatedMenuItem.getName(), updatedMenuItem.getDescription(), updatedMenuItem.getPrice(), updatedMenuItem.getCategory(), updatedMenuItem.getAvailable(), updatedMenuItem.getImageUrl(), updatedMenuItem.getCreatedAt());
        when(menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)).thenReturn(Optional.of(menuItem));
        when(securityService.isOwnerOrAdmin(currentUserId, ownerId)).thenReturn(true);
        when(menuItemRepository.save(menuItem)).thenReturn(updatedMenuItem);
        when(menuItemMapper.toResponse(updatedMenuItem)).thenReturn(expectedResponse);
        MenuItemResponse result = menuItemService.updateMenuItem(restaurantId, menuItemId, request, currentUserId);
        assertThat(result, equalTo(expectedResponse));
        verify(menuItemRepository, atLeast(1)).findByIdAndRestaurantId(menuItemId, restaurantId);
        verify(securityService, atLeast(1)).isOwnerOrAdmin(currentUserId, ownerId);
        verify(menuItemMapper, atLeast(1)).updateEntityFromRequest(request, menuItem);
        verify(menuItemRepository, atLeast(1)).save(menuItem);
        verify(menuItemMapper, atLeast(1)).toResponse(updatedMenuItem);
    }

    @Test
    void testUpdateMenuItemThrowsMenuItemNotFoundException() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID menuItemId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        UUID currentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        UpdateMenuItemRequest request = new UpdateMenuItemRequest("Updated Burger", "Updated description", new BigDecimal("12.99"), "Updated Category", true, "http://example.com/updated-burger.jpg");
        when(menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)).thenReturn(Optional.empty());
        MenuItemNotFoundException exception = assertThrows(MenuItemNotFoundException.class, () -> menuItemService.updateMenuItem(restaurantId, menuItemId, request, currentUserId));
        assertThat(exception.getMessage(), equalTo("Menu item not found"));
        verify(menuItemRepository, atLeast(1)).findByIdAndRestaurantId(menuItemId, restaurantId);
    }

    @Test
    void testUpdateMenuItemThrowsUnauthorizedOperationException() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID menuItemId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        UUID currentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        UUID ownerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174003");
        UpdateMenuItemRequest request = new UpdateMenuItemRequest("Updated Burger", "Updated description", new BigDecimal("12.99"), "Updated Category", true, "http://example.com/updated-burger.jpg");
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setOwnerId(ownerId);
        restaurant.setName("Test Restaurant");
        MenuItem menuItem = new MenuItem();
        menuItem.setId(menuItemId);
        menuItem.setName("Burger");
        menuItem.setDescription("Delicious burger");
        menuItem.setPrice(new BigDecimal("10.99"));
        menuItem.setCategory("Main Course");
        menuItem.setAvailable(true);
        menuItem.setRestaurant(restaurant);
        menuItem.setCreatedAt(LocalDateTime.now());
        when(menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)).thenReturn(Optional.of(menuItem));
        when(securityService.isOwnerOrAdmin(currentUserId, ownerId)).thenReturn(false);
        UnauthorizedOperationException exception = assertThrows(UnauthorizedOperationException.class, () -> menuItemService.updateMenuItem(restaurantId, menuItemId, request, currentUserId));
        assertThat(exception.getMessage(), equalTo("You are not authorized to update this menu item"));
        verify(menuItemRepository, atLeast(1)).findByIdAndRestaurantId(menuItemId, restaurantId);
        verify(securityService, atLeast(1)).isOwnerOrAdmin(currentUserId, ownerId);
    }

    @Test
    void testDeleteMenuItemSuccessfully() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID menuItemId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        UUID currentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        UUID ownerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setOwnerId(ownerId);
        restaurant.setName("Test Restaurant");
        MenuItem menuItem = new MenuItem();
        menuItem.setId(menuItemId);
        menuItem.setName("Burger");
        menuItem.setDescription("Delicious burger");
        menuItem.setPrice(new BigDecimal("10.99"));
        menuItem.setCategory("Main Course");
        menuItem.setAvailable(true);
        menuItem.setRestaurant(restaurant);
        menuItem.setCreatedAt(LocalDateTime.now());
        when(menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)).thenReturn(Optional.of(menuItem));
        when(securityService.isOwnerOrAdmin(currentUserId, ownerId)).thenReturn(true);
        menuItemService.deleteMenuItem(restaurantId, menuItemId, currentUserId);
        verify(menuItemRepository, atLeast(1)).findByIdAndRestaurantId(menuItemId, restaurantId);
        verify(securityService, atLeast(1)).isOwnerOrAdmin(currentUserId, ownerId);
        verify(menuItemRepository, atLeast(1)).delete(menuItem);
    }

    @Test
    void testDeleteMenuItemThrowsMenuItemNotFoundException() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID menuItemId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        UUID currentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        when(menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)).thenReturn(Optional.empty());
        MenuItemNotFoundException exception = assertThrows(MenuItemNotFoundException.class, () -> menuItemService.deleteMenuItem(restaurantId, menuItemId, currentUserId));
        assertThat(exception.getMessage(), equalTo("Menu item not found"));
        verify(menuItemRepository, atLeast(1)).findByIdAndRestaurantId(menuItemId, restaurantId);
    }

    @Test
    void testDeleteMenuItemThrowsUnauthorizedOperationException() {
        UUID restaurantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID menuItemId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        UUID currentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        UUID ownerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174003");
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setOwnerId(ownerId);
        restaurant.setName("Test Restaurant");
        MenuItem menuItem = new MenuItem();
        menuItem.setId(menuItemId);
        menuItem.setName("Burger");
        menuItem.setDescription("Delicious burger");
        menuItem.setPrice(new BigDecimal("10.99"));
        menuItem.setCategory("Main Course");
        menuItem.setAvailable(true);
        menuItem.setRestaurant(restaurant);
        menuItem.setCreatedAt(LocalDateTime.now());
        when(menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)).thenReturn(Optional.of(menuItem));
        when(securityService.isOwnerOrAdmin(currentUserId, ownerId)).thenReturn(false);
        UnauthorizedOperationException exception = assertThrows(UnauthorizedOperationException.class, () -> menuItemService.deleteMenuItem(restaurantId, menuItemId, currentUserId));
        assertThat(exception.getMessage(), equalTo("You are not authorized to delete this menu item"));
        verify(menuItemRepository, atLeast(1)).findByIdAndRestaurantId(menuItemId, restaurantId);
        verify(securityService, atLeast(1)).isOwnerOrAdmin(currentUserId, ownerId);
    }

    @Test
    void testConstructorInitializesAllFields() {
        MenuItemService service = new MenuItemService(menuItemRepository, restaurantRepository, menuItemMapper, securityService);
        assertThat(service, notNullValue());
    }
}
