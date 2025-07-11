package com.bytebites.restaurantservice.controller;

import com.bytebites.restaurantservice.dto.CreateMenuItemRequest;
import com.bytebites.restaurantservice.dto.MenuItemResponse;
import com.bytebites.restaurantservice.dto.UpdateMenuItemRequest;
import com.bytebites.restaurantservice.service.MenuItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;

@Timeout(10)
public class MenuItemControllerTest {

    @Mock
    private MenuItemService menuItemService;

    private MenuItemController menuItemController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        menuItemController = new MenuItemController(menuItemService);
    }

    @Test
    public void testGetMenuReturnsMenuItemsList() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        MenuItemResponse menuItem1 = new MenuItemResponse(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), "Pizza", "Delicious pizza", new BigDecimal("12.99"), "Main Course", true, "https://example.com/pizza.jpg", LocalDateTime.now());
        MenuItemResponse menuItem2 = new MenuItemResponse(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"), "Salad", "Fresh salad", new BigDecimal("8.50"), "Appetizer", true, "https://example.com/salad.jpg", LocalDateTime.now());
        List<MenuItemResponse> menuItems = new ArrayList<>();
        menuItems.add(menuItem1);
        menuItems.add(menuItem2);
        doReturn(menuItems).when(menuItemService).getMenuByRestaurantId(eq(restaurantId));
        ResponseEntity<List<MenuItemResponse>> response = menuItemController.getMenu(restaurantId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(menuItem1, response.getBody().get(0));
        assertEquals(menuItem2, response.getBody().get(1));
        verify(menuItemService, atLeast(1)).getMenuByRestaurantId(eq(restaurantId));
    }

    @Test
    public void testGetMenuReturnsEmptyList() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        List<MenuItemResponse> emptyList = new ArrayList<>();
        doReturn(emptyList).when(menuItemService).getMenuByRestaurantId(eq(restaurantId));
        ResponseEntity<List<MenuItemResponse>> response = menuItemController.getMenu(restaurantId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(menuItemService, atLeast(1)).getMenuByRestaurantId(eq(restaurantId));
    }

    @Test
    public void testAddMenuItemReturnsCreatedMenuItem() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        String userIdString = userId.toString();
        CreateMenuItemRequest request = new CreateMenuItemRequest("Burger", "Tasty burger", new BigDecimal("9.99"), "Main Course", "https://example.com/burger.jpg");
        MenuItemResponse expectedResponse = new MenuItemResponse(UUID.fromString("550e8400-e29b-41d4-a716-446655440004"), "Burger", "Tasty burger", new BigDecimal("9.99"), "Main Course", true, "https://example.com/burger.jpg", LocalDateTime.now());
        doReturn(expectedResponse).when(menuItemService).addMenuItem(eq(restaurantId), eq(request), eq(userId));
        ResponseEntity<MenuItemResponse> response = menuItemController.addMenuItem(restaurantId, request, userIdString);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
        verify(menuItemService, atLeast(1)).addMenuItem(eq(restaurantId), eq(request), eq(userId));
    }

    @Test
    public void testGetMenuItemReturnsMenuItem() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID itemId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        MenuItemResponse expectedResponse = new MenuItemResponse(itemId, "Pizza", "Delicious pizza", new BigDecimal("12.99"), "Main Course", true, "https://example.com/pizza.jpg", LocalDateTime.now());
        doReturn(expectedResponse).when(menuItemService).getMenuItem(eq(restaurantId), eq(itemId));
        ResponseEntity<MenuItemResponse> response = menuItemController.getMenuItem(restaurantId, itemId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
        verify(menuItemService, atLeast(1)).getMenuItem(eq(restaurantId), eq(itemId));
    }

    @Test
    public void testUpdateMenuItemReturnsUpdatedMenuItem() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID itemId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        String userIdString = userId.toString();
        UpdateMenuItemRequest request = new UpdateMenuItemRequest("Updated Pizza", "Updated description", new BigDecimal("15.99"), "Main Course", false, "https://example.com/updated-pizza.jpg");
        MenuItemResponse expectedResponse = new MenuItemResponse(itemId, "Updated Pizza", "Updated description", new BigDecimal("15.99"), "Main Course", false, "https://example.com/updated-pizza.jpg", LocalDateTime.now());
        doReturn(expectedResponse).when(menuItemService).updateMenuItem(eq(restaurantId), eq(itemId), eq(request), eq(userId));
        ResponseEntity<MenuItemResponse> response = menuItemController.updateMenuItem(restaurantId, itemId, request, userIdString);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
        verify(menuItemService, atLeast(1)).updateMenuItem(eq(restaurantId), eq(itemId), eq(request), eq(userId));
    }

    @Test
    public void testDeleteMenuItemReturnsNoContent() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID itemId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        String userIdString = userId.toString();
        doNothing().when(menuItemService).deleteMenuItem(eq(restaurantId), eq(itemId), eq(userId));
        ResponseEntity<Void> response = menuItemController.deleteMenuItem(restaurantId, itemId, userIdString);
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(menuItemService, atLeast(1)).deleteMenuItem(eq(restaurantId), eq(itemId), eq(userId));
    }

    @Test
    public void testConstructorInitializesController() {
        MenuItemService mockService = mock(MenuItemService.class);
        MenuItemController controller = new MenuItemController(mockService);
        assertNotNull(controller);
    }

    @Test
    public void testAddMenuItemWithDifferentRequestValues() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        String userIdString = userId.toString();
        CreateMenuItemRequest request = new CreateMenuItemRequest("Pasta", "Italian pasta", new BigDecimal("11.50"), "Main Course", null);
        MenuItemResponse expectedResponse = new MenuItemResponse(UUID.fromString("550e8400-e29b-41d4-a716-446655440005"), "Pasta", "Italian pasta", new BigDecimal("11.50"), "Main Course", true, null, LocalDateTime.now());
        doReturn(expectedResponse).when(menuItemService).addMenuItem(eq(restaurantId), eq(request), eq(userId));
        ResponseEntity<MenuItemResponse> response = menuItemController.addMenuItem(restaurantId, request, userIdString);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.CREATED)));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody(), is(equalTo(expectedResponse)));
        verify(menuItemService, atLeast(1)).addMenuItem(eq(restaurantId), eq(request), eq(userId));
    }

    @Test
    public void testUpdateMenuItemWithNullValues() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID itemId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        String userIdString = userId.toString();
        UpdateMenuItemRequest request = new UpdateMenuItemRequest(null, null, null, null, null, null);
        MenuItemResponse expectedResponse = new MenuItemResponse(itemId, "Pizza", "Delicious pizza", new BigDecimal("12.99"), "Main Course", true, "https://example.com/pizza.jpg", LocalDateTime.now());
        doReturn(expectedResponse).when(menuItemService).updateMenuItem(eq(restaurantId), eq(itemId), eq(request), eq(userId));
        ResponseEntity<MenuItemResponse> response = menuItemController.updateMenuItem(restaurantId, itemId, request, userIdString);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody(), is(equalTo(expectedResponse)));
        verify(menuItemService, atLeast(1)).updateMenuItem(eq(restaurantId), eq(itemId), eq(request), eq(userId));
    }
}
