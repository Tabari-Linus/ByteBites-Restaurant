package com.bytebites.restaurantservice.service;

import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.dto.RestaurantResponse;
import com.bytebites.restaurantservice.dto.UpdateRestaurantRequest;
import com.bytebites.restaurantservice.enums.RestaurantStatus;
import com.bytebites.restaurantservice.event.RestaurantEventPublisher;
import com.bytebites.restaurantservice.exception.RestaurantAlreadyExit;
import com.bytebites.restaurantservice.exception.RestaurantNotFoundException;
import com.bytebites.restaurantservice.exception.UnauthorizedOperationException;
import com.bytebites.restaurantservice.mapper.RestaurantMapper;
import com.bytebites.restaurantservice.model.Restaurant;
import com.bytebites.restaurantservice.repository.RestaurantRepository;
import com.bytebites.restaurantservice.security.SecurityService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantMapper restaurantMapper;

    @Mock
    private SecurityService securityService;

    @Mock
    private RestaurantEventPublisher restaurantEventPublisher;

    private RestaurantService restaurantService;

    private UUID ownerId;
    private UUID restaurantId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        MockitoAnnotations.openMocks(this);
        restaurantService = new RestaurantService(restaurantRepository, restaurantMapper, securityService, restaurantEventPublisher);
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
        mockRestaurant.setOwnerId(ownerId); 
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

    @Test
    public void createRestaurantShouldReturnRestaurantResponseWhenValidRequest() {
        
        CreateRestaurantRequest request = new CreateRestaurantRequest("Test Restaurant", "Description", "Address", "+1234567890", "test@example.com");
        UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        restaurant.setName("Test Restaurant");
        restaurant.setStatus(RestaurantStatus.PENDING_APPROVAL);
        Restaurant savedRestaurant = new Restaurant();
        savedRestaurant.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        savedRestaurant.setName("Test Restaurant");
        savedRestaurant.setStatus(RestaurantStatus.PENDING_APPROVAL);
        RestaurantResponse expectedResponse = new RestaurantResponse(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), "Test Restaurant", "Description", "Address", "+1234567890", "test@example.com", RestaurantStatus.PENDING_APPROVAL, "Restaurant Owner", LocalDateTime.now(), new ArrayList<>());
        doReturn(false).when(restaurantRepository).existsByOwnerIdAndName(eq(ownerId), eq("Test Restaurant"));
        doReturn(restaurant).when(restaurantMapper).toEntity(eq(request), eq(ownerId));
        doReturn(savedRestaurant).when(restaurantRepository).save(any(Restaurant.class));
        doReturn(expectedResponse).when(restaurantMapper).toResponse(eq(savedRestaurant));
        
        RestaurantResponse result = restaurantService.createRestaurant(request, ownerId);
        
        assertNotNull(result);
        assertEquals("Test Restaurant", result.name());
        assertEquals(RestaurantStatus.PENDING_APPROVAL, result.status());
        verify(restaurantEventPublisher, atLeast(1)).publishRestaurantCreatedEvent(eq(savedRestaurant));
        verify(restaurantRepository, atLeast(1)).save(any(Restaurant.class));
    }

    @Test
    public void createRestaurantShouldThrowRestaurantAlreadyExitWhenRestaurantExists() {
        
        CreateRestaurantRequest request = new CreateRestaurantRequest("Test Restaurant", "Description", "Address", "+1234567890", "test@example.com");
        UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        doReturn(true).when(restaurantRepository).existsByOwnerIdAndName(eq(ownerId), eq("Test Restaurant"));
        
        assertThrows(RestaurantAlreadyExit.class, () -> restaurantService.createRestaurant(request, ownerId));
    }

    @Test
    public void getAllActiveRestaurantsShouldReturnListOfRestaurants() {
        
        List<Restaurant> restaurants = new ArrayList<>();
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        restaurant1.setName("Restaurant 1");
        restaurant1.setStatus(RestaurantStatus.ACTIVE);
        restaurants.add(restaurant1);
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        restaurant2.setName("Restaurant 2");
        restaurant2.setStatus(RestaurantStatus.ACTIVE);
        restaurants.add(restaurant2);
        List<RestaurantResponse> expectedResponses = new ArrayList<>();
        expectedResponses.add(new RestaurantResponse(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), "Restaurant 1", "Description", "Address", "+1234567890", "test@example.com", RestaurantStatus.ACTIVE, "Restaurant Owner", LocalDateTime.now(), new ArrayList<>()));
        expectedResponses.add(new RestaurantResponse(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), "Restaurant 2", "Description", "Address", "+1234567890", "test@example.com", RestaurantStatus.ACTIVE, "Restaurant Owner", LocalDateTime.now(), new ArrayList<>()));
        doReturn(restaurants).when(restaurantRepository).findActiveRestaurantsOrderByCreatedAt(eq(RestaurantStatus.ACTIVE));
        doReturn(expectedResponses).when(restaurantMapper).toResponseList(eq(restaurants));
        
        List<RestaurantResponse> result = restaurantService.getAllActiveRestaurants();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Restaurant 1", result.get(0).name());
        assertEquals("Restaurant 2", result.get(1).name());
    }

    @Test
    public void getAllActiveRestaurantsShouldReturnEmptyListWhenNoActiveRestaurants() {
        
        List<Restaurant> restaurants = new ArrayList<>();
        List<RestaurantResponse> expectedResponses = new ArrayList<>();
        doReturn(restaurants).when(restaurantRepository).findActiveRestaurantsOrderByCreatedAt(eq(RestaurantStatus.ACTIVE));
        doReturn(expectedResponses).when(restaurantMapper).toResponseList(eq(restaurants));
        
        List<RestaurantResponse> result = restaurantService.getAllActiveRestaurants();
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getAllRestaurantsShouldReturnPageOfRestaurants() {
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Restaurant> restaurants = new ArrayList<>();
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        restaurant1.setName("Restaurant 1");
        restaurants.add(restaurant1);
        Page<Restaurant> restaurantPage = new PageImpl<>(restaurants, pageable, 1);
        RestaurantResponse response1 = new RestaurantResponse(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), "Restaurant 1", "Description", "Address", "+1234567890", "test@example.com", RestaurantStatus.ACTIVE, "Restaurant Owner", LocalDateTime.now(), new ArrayList<>());
        doReturn(restaurantPage).when(restaurantRepository).findAll(eq(pageable));
        doReturn(response1).when(restaurantMapper).toResponseWithoutMenuItems(eq(restaurant1));
        
        Page<RestaurantResponse> result = restaurantService.getAllRestaurants(pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Restaurant 1", result.getContent().get(0).name());
    }

    @Test
    public void getRestaurantByIdShouldReturnRestaurantWhenExists() {
        
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Test Restaurant");
        RestaurantResponse expectedResponse = new RestaurantResponse(restaurantId, "Test Restaurant", "Description", "Address", "+1234567890", "test@example.com", RestaurantStatus.ACTIVE, "Restaurant Owner", LocalDateTime.now(), new ArrayList<>());
        doReturn(Optional.of(restaurant)).when(restaurantRepository).findByIdWithMenuItems(eq(restaurantId));
        doReturn(expectedResponse).when(restaurantMapper).toResponse(eq(restaurant));
        
        RestaurantResponse result = restaurantService.getRestaurantById(restaurantId);
        
        assertNotNull(result);
        assertEquals("Test Restaurant", result.name());
        assertEquals(restaurantId, result.id());
    }

    @Test
    public void getRestaurantByIdShouldThrowRestaurantNotFoundExceptionWhenNotExists() {
        
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        doReturn(Optional.empty()).when(restaurantRepository).findByIdWithMenuItems(eq(restaurantId));
        
        assertThrows(RestaurantNotFoundException.class, () -> restaurantService.getRestaurantById(restaurantId));
    }

    @Test
    public void getMyRestaurantsShouldReturnListOfRestaurantsForOwner() {
        
        UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        List<Restaurant> restaurants = new ArrayList<>();
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        restaurant1.setName("Owner Restaurant 1");
        restaurant1.setOwnerId(ownerId);
        restaurants.add(restaurant1);
        List<RestaurantResponse> expectedResponses = new ArrayList<>();
        expectedResponses.add(new RestaurantResponse(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), "Owner Restaurant 1", "Description", "Address", "+1234567890", "test@example.com", RestaurantStatus.ACTIVE, "Restaurant Owner", LocalDateTime.now(), new ArrayList<>()));
        doReturn(restaurants).when(restaurantRepository).findByOwnerId(eq(ownerId));
        doReturn(expectedResponses).when(restaurantMapper).toResponseList(eq(restaurants));
        
        List<RestaurantResponse> result = restaurantService.getMyRestaurants(ownerId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Owner Restaurant 1", result.get(0).name());
    }

    @Test
    public void getMyRestaurantsShouldReturnEmptyListWhenOwnerHasNoRestaurants() {
        
        UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        List<Restaurant> restaurants = new ArrayList<>();
        List<RestaurantResponse> expectedResponses = new ArrayList<>();
        doReturn(restaurants).when(restaurantRepository).findByOwnerId(eq(ownerId));
        doReturn(expectedResponses).when(restaurantMapper).toResponseList(eq(restaurants));
        
        List<RestaurantResponse> result = restaurantService.getMyRestaurants(ownerId);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void updateRestaurantShouldReturnUpdatedRestaurantWhenAuthorized() {
        
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID currentUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UpdateRestaurantRequest request = new UpdateRestaurantRequest("Updated Restaurant", "Updated Description", "Updated Address", "+1234567890", "updated@example.com", RestaurantStatus.ACTIVE);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Original Restaurant");
        restaurant.setOwnerId(ownerId);
        Restaurant updatedRestaurant = new Restaurant();
        updatedRestaurant.setId(restaurantId);
        updatedRestaurant.setName("Updated Restaurant");
        updatedRestaurant.setOwnerId(ownerId);
        RestaurantResponse expectedResponse = new RestaurantResponse(restaurantId, "Updated Restaurant", "Updated Description", "Updated Address", "+1234567890", "updated@example.com", RestaurantStatus.ACTIVE, "Restaurant Owner", LocalDateTime.now(), new ArrayList<>());
        doReturn(Optional.of(restaurant)).when(restaurantRepository).findById(eq(restaurantId));
        doReturn(true).when(securityService).isOwnerOrAdmin(eq(currentUserId), eq(ownerId));
        doReturn(updatedRestaurant).when(restaurantRepository).save(any(Restaurant.class));
        doReturn(expectedResponse).when(restaurantMapper).toResponse(eq(updatedRestaurant));
        
        RestaurantResponse result = restaurantService.updateRestaurant(restaurantId, request, currentUserId);
        
        assertNotNull(result);
        assertEquals("Updated Restaurant", result.name());
        verify(restaurantMapper, atLeast(1)).updateEntityFromRequest(eq(request), eq(restaurant));
    }

    @Test
    public void updateRestaurantShouldThrowRestaurantNotFoundExceptionWhenRestaurantNotExists() {
        
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID currentUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UpdateRestaurantRequest request = new UpdateRestaurantRequest("Updated Restaurant", "Updated Description", "Updated Address", "+1234567890", "updated@example.com", RestaurantStatus.ACTIVE);
        doReturn(Optional.empty()).when(restaurantRepository).findById(eq(restaurantId));
        
        assertThrows(RestaurantNotFoundException.class, () -> restaurantService.updateRestaurant(restaurantId, request, currentUserId));
    }

    @Test
    public void updateRestaurantShouldThrowUnauthorizedOperationExceptionWhenNotAuthorized() {
        
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID currentUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        UpdateRestaurantRequest request = new UpdateRestaurantRequest("Updated Restaurant", "Updated Description", "Updated Address", "+1234567890", "updated@example.com", RestaurantStatus.ACTIVE);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Original Restaurant");
        restaurant.setOwnerId(ownerId);
        doReturn(Optional.of(restaurant)).when(restaurantRepository).findById(eq(restaurantId));
        doReturn(false).when(securityService).isOwnerOrAdmin(eq(currentUserId), eq(ownerId));
        
        assertThrows(UnauthorizedOperationException.class, () -> restaurantService.updateRestaurant(restaurantId, request, currentUserId));
    }

    @Test
    public void updateRestaurantStatusShouldReturnUpdatedRestaurantWhenUserIsAdmin() {
        
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID currentUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        UpdateRestaurantRequest request = new UpdateRestaurantRequest("Updated Restaurant", "Updated Description", "Updated Address", "+1234567890", "updated@example.com", RestaurantStatus.ACTIVE);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Original Restaurant");
        restaurant.setOwnerId(ownerId);
        restaurant.setStatus(RestaurantStatus.PENDING_APPROVAL);
        Restaurant updatedRestaurant = new Restaurant();
        updatedRestaurant.setId(restaurantId);
        updatedRestaurant.setName("Updated Restaurant");
        updatedRestaurant.setOwnerId(ownerId);
        updatedRestaurant.setStatus(RestaurantStatus.ACTIVE);
        RestaurantResponse expectedResponse = new RestaurantResponse(restaurantId, "Updated Restaurant", "Updated Description", "Updated Address", "+1234567890", "updated@example.com", RestaurantStatus.ACTIVE, "Restaurant Owner", LocalDateTime.now(), new ArrayList<>());
        doReturn(Optional.of(restaurant)).when(restaurantRepository).findById(eq(restaurantId));
        doReturn(true).when(securityService).isAdmin(eq(currentUserId));
        doReturn(updatedRestaurant).when(restaurantRepository).save(any(Restaurant.class));
        doReturn(expectedResponse).when(restaurantMapper).toResponse(eq(updatedRestaurant));
        
        RestaurantResponse result = restaurantService.updateRestaurantStatus(restaurantId, request, currentUserId);
        
        assertNotNull(result);
        assertEquals("Updated Restaurant", result.name());
        assertEquals(RestaurantStatus.ACTIVE, result.status());
        verify(restaurantMapper, atLeast(1)).updateEntityFromRequest(eq(request), eq(restaurant));
    }

    @Test
    public void updateRestaurantStatusShouldThrowRestaurantNotFoundExceptionWhenRestaurantNotExists() {
        
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID currentUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UpdateRestaurantRequest request = new UpdateRestaurantRequest("Updated Restaurant", "Updated Description", "Updated Address", "+1234567890", "updated@example.com", RestaurantStatus.ACTIVE);
        doReturn(Optional.empty()).when(restaurantRepository).findById(eq(restaurantId));
        
        assertThrows(RestaurantNotFoundException.class, () -> restaurantService.updateRestaurantStatus(restaurantId, request, currentUserId));
    }

    @Test
    public void updateRestaurantStatusShouldThrowUnauthorizedOperationExceptionWhenNotAdmin() {
        
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID currentUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        UpdateRestaurantRequest request = new UpdateRestaurantRequest("Updated Restaurant", "Updated Description", "Updated Address", "+1234567890", "updated@example.com", RestaurantStatus.ACTIVE);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Original Restaurant");
        restaurant.setOwnerId(ownerId);
        doReturn(Optional.of(restaurant)).when(restaurantRepository).findById(eq(restaurantId));
        doReturn(false).when(securityService).isAdmin(eq(currentUserId));
        
        assertThrows(UnauthorizedOperationException.class, () -> restaurantService.updateRestaurantStatus(restaurantId, request, currentUserId));
    }

    @Test
    public void deleteRestaurantShouldSoftDeleteRestaurantWhenAuthorized() {
        
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID currentUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Test Restaurant");
        restaurant.setOwnerId(ownerId);
        restaurant.setStatus(RestaurantStatus.ACTIVE);
        doReturn(Optional.of(restaurant)).when(restaurantRepository).findById(eq(restaurantId));
        doReturn(true).when(securityService).isOwnerOrAdmin(eq(currentUserId), eq(ownerId));
        doReturn(restaurant).when(restaurantRepository).save(any(Restaurant.class));
        
        restaurantService.deleteRestaurant(restaurantId, currentUserId);
        
        verify(restaurantRepository, atLeast(1)).save(any(Restaurant.class));
        assertThat(restaurant.getStatus(), equalTo(RestaurantStatus.INACTIVE));
    }

    @Test
    public void deleteRestaurantShouldThrowRestaurantNotFoundExceptionWhenRestaurantNotExists() {
        
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID currentUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        doReturn(Optional.empty()).when(restaurantRepository).findById(eq(restaurantId));
        
        assertThrows(RestaurantNotFoundException.class, () -> restaurantService.deleteRestaurant(restaurantId, currentUserId));
    }

    @Test
    public void deleteRestaurantShouldThrowUnauthorizedOperationExceptionWhenNotAuthorized() {
        
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID currentUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Test Restaurant");
        restaurant.setOwnerId(ownerId);
        restaurant.setStatus(RestaurantStatus.ACTIVE);
        doReturn(Optional.of(restaurant)).when(restaurantRepository).findById(eq(restaurantId));
        doReturn(false).when(securityService).isOwnerOrAdmin(eq(currentUserId), eq(ownerId));
        
        assertThrows(UnauthorizedOperationException.class, () -> restaurantService.deleteRestaurant(restaurantId, currentUserId));
    }

    @Test
    public void constructorShouldInitializeAllDependencies() {
        
        RestaurantRepository mockRepository = mock(RestaurantRepository.class);
        RestaurantMapper mockMapper = mock(RestaurantMapper.class);
        SecurityService mockSecurityService = mock(SecurityService.class);
        RestaurantEventPublisher mockEventPublisher = mock(RestaurantEventPublisher.class);
        
        RestaurantService service = new RestaurantService(mockRepository, mockMapper, mockSecurityService, mockEventPublisher);
        
        assertThat(service, notNullValue());
    }
}