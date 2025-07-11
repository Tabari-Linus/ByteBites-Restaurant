package com.bytebites.restaurantservice.controller;

import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.dto.RestaurantResponse;
import com.bytebites.restaurantservice.dto.UpdateRestaurantRequest;
import com.bytebites.restaurantservice.service.RestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RestaurantService restaurantService;

    private RestaurantController restaurantController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        MockitoAnnotations.openMocks(this);
        restaurantController = new RestaurantController(restaurantService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(restaurantController)
                .build();
        }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void shouldCreateRestaurant() throws Exception {
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Pizza Palace",
                "Best pizza in town",
                "123 Main St",
                "+1234567890",
                "contact@pizzapalace.com"
        );

        mockMvc.perform(post("/api/restaurants")
                        .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetAllRestaurants() throws Exception {
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateRestaurant() {
        CreateRestaurantRequest request = new CreateRestaurantRequest("Test Restaurant", "Test Description", "Test Address", "+1234567890", "test@example.com");
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UUID userUuid = UUID.fromString(userId);
        RestaurantResponse expectedResponse = mock(RestaurantResponse.class);
        doReturn(expectedResponse).when(restaurantService).createRestaurant(eq(request), eq(userUuid));
        ResponseEntity<RestaurantResponse> result = restaurantController.createRestaurant(request, userId);
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(restaurantService, atLeast(1)).createRestaurant(eq(request), eq(userUuid));
    }

    @Test
    void testGetAllRestaurants() {
        RestaurantResponse response1 = mock(RestaurantResponse.class);
        RestaurantResponse response2 = mock(RestaurantResponse.class);
        List<RestaurantResponse> expectedList = Arrays.asList(response1, response2);
        doReturn(expectedList).when(restaurantService).getAllActiveRestaurants();
        ResponseEntity<List<RestaurantResponse>> result = restaurantController.getAllRestaurants();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedList, result.getBody());
        verify(restaurantService, atLeast(1)).getAllActiveRestaurants();
    }

    @Test
    void testGetAllRestaurantsPaged() {
        Pageable pageable = mock(Pageable.class);
        RestaurantResponse response1 = mock(RestaurantResponse.class);
        RestaurantResponse response2 = mock(RestaurantResponse.class);
        List<RestaurantResponse> content = Arrays.asList(response1, response2);
        Page<RestaurantResponse> expectedPage = new PageImpl<>(content);
        doReturn(expectedPage).when(restaurantService).getAllRestaurants(eq(pageable));
        ResponseEntity<Page<RestaurantResponse>> result = restaurantController.getAllRestaurantsPaged(pageable);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedPage, result.getBody());
        verify(restaurantService, atLeast(1)).getAllRestaurants(eq(pageable));
    }

    @Test
    void testGetRestaurantById() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        RestaurantResponse expectedResponse = mock(RestaurantResponse.class);
        doReturn(expectedResponse).when(restaurantService).getRestaurantById(eq(restaurantId));
        ResponseEntity<RestaurantResponse> result = restaurantController.getRestaurantById(restaurantId);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(restaurantService, atLeast(1)).getRestaurantById(eq(restaurantId));
    }

    @Test
    void testGetMyRestaurants() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UUID userUuid = UUID.fromString(userId);
        RestaurantResponse response1 = mock(RestaurantResponse.class);
        RestaurantResponse response2 = mock(RestaurantResponse.class);
        List<RestaurantResponse> expectedList = Arrays.asList(response1, response2);
        doReturn(expectedList).when(restaurantService).getMyRestaurants(eq(userUuid));
        ResponseEntity<List<RestaurantResponse>> result = restaurantController.getMyRestaurants(userId);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedList, result.getBody());
        verify(restaurantService, atLeast(1)).getMyRestaurants(eq(userUuid));
    }

    @Test
    void testUpdateRestaurant() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UpdateRestaurantRequest request = new UpdateRestaurantRequest("Updated Restaurant", "Updated Description", "Updated Address", "+9876543210", "updated@example.com", null);
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UUID userUuid = UUID.fromString(userId);
        RestaurantResponse expectedResponse = mock(RestaurantResponse.class);
        doReturn(expectedResponse).when(restaurantService).updateRestaurant(eq(restaurantId), eq(request), eq(userUuid));
        ResponseEntity<RestaurantResponse> result = restaurantController.updateRestaurant(restaurantId, request, userId);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(restaurantService, atLeast(1)).updateRestaurant(eq(restaurantId), eq(request), eq(userUuid));
    }

    @Test
    void testUpdateRestaurantStatus() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UpdateRestaurantRequest request = new UpdateRestaurantRequest(null, null, null, null, null, null);
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UUID userUuid = UUID.fromString(userId);
        RestaurantResponse expectedResponse = mock(RestaurantResponse.class);
        doReturn(expectedResponse).when(restaurantService).updateRestaurantStatus(eq(restaurantId), eq(request), eq(userUuid));
        ResponseEntity<RestaurantResponse> result = restaurantController.updateRestaurantStatus(restaurantId, request, userId);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(restaurantService, atLeast(1)).updateRestaurantStatus(eq(restaurantId), eq(request), eq(userUuid));
    }

    @Test
    void testDeleteRestaurant() {
        UUID restaurantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UUID userUuid = UUID.fromString(userId);
        doNothing().when(restaurantService).deleteRestaurant(eq(restaurantId), eq(userUuid));
        ResponseEntity<Void> result = restaurantController.deleteRestaurant(restaurantId, userId);
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(restaurantService, atLeast(1)).deleteRestaurant(eq(restaurantId), eq(userUuid));
    }

    @Test
    void testConstructor() {
        RestaurantService mockService = mock(RestaurantService.class);
        RestaurantController controller = new RestaurantController(mockService);
        assertThat(controller, is(notNullValue()));
    }
}