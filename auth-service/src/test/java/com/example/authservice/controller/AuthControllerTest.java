package com.example.authservice.controller;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.LoginResponse;
import com.example.authservice.dto.RefreshTokenRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.SuccessMessage;
import com.example.authservice.dto.UserInfo;
import com.example.authservice.enums.RoleName;
import com.example.authservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;

@Timeout(10)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    private AuthController authController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(userService);
    }

    @Test
    public void testRegisterSuccess() {
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "password123", "John", "Doe", RoleName.ROLE_CUSTOMER);
        SuccessMessage successMessage = new SuccessMessage("User registered successfully");
        doReturn(successMessage).when(userService).register(any(RegisterRequest.class));
        ResponseEntity<SuccessMessage> response = authController.register(registerRequest);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User registered successfully", response.getBody().message());
        verify(userService, atLeast(1)).register(eq(registerRequest));
    }

    @Test
    public void testLoginSuccess() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        LoginResponse loginResponse = new LoginResponse("access-token", "refresh-token", "Bearer");
        doReturn(loginResponse).when(userService).login(any(LoginRequest.class));
        ResponseEntity<LoginResponse> response = authController.login(loginRequest);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access-token", response.getBody().accessToken());
        assertEquals("refresh-token", response.getBody().refreshToken());
        assertEquals("Bearer", response.getBody().tokenType());
        verify(userService, atLeast(1)).login(eq(loginRequest));
    }

    @Test
    public void testRefreshTokenSuccess() {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("refresh-token");
        LoginResponse loginResponse = new LoginResponse("new-access-token", "new-refresh-token", "Bearer");
        doReturn(loginResponse).when(userService).refreshToken(any(RefreshTokenRequest.class));
        ResponseEntity<LoginResponse> response = authController.refreshToken(refreshTokenRequest);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new-access-token", response.getBody().accessToken());
        assertEquals("new-refresh-token", response.getBody().refreshToken());
        assertEquals("Bearer", response.getBody().tokenType());
        verify(userService, atLeast(1)).refreshToken(eq(refreshTokenRequest));
    }

    @Test
    public void testGetCurrentUserSuccess() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UUID userUUID = UUID.fromString(userId);
        UserInfo userInfo = new UserInfo(userUUID, "test@example.com", "John", "Doe", Set.of("USER"));
        doReturn(userInfo).when(userService).getCurrentUser(any(UUID.class));
        ResponseEntity<UserInfo> response = authController.getCurrentUser(userId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userUUID, response.getBody().id());
        assertEquals("test@example.com", response.getBody().email());
        assertEquals("John", response.getBody().firstName());
        assertEquals("Doe", response.getBody().lastName());
        assertThat(response.getBody().roles(), is(notNullValue()));
        verify(userService, atLeast(1)).getCurrentUser(eq(userUUID));
    }

    @Test
    public void testGetRolesSuccess() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UUID userUUID = UUID.fromString(userId);
        Set<String> roles = Set.of("USER", "ADMIN");
        doReturn(roles).when(userService).getRoles(any(UUID.class));
        ResponseEntity<Set<String>> response = authController.getRoles(userId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertThat(response.getBody(), is(notNullValue()));
        verify(userService, atLeast(1)).getRoles(eq(userUUID));
    }

    @Test
    public void testLogoutSuccess() {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("refresh-token");
        doNothing().when(userService).logout(any(String.class));
        ResponseEntity<Void> response = authController.logout(refreshTokenRequest);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, atLeast(1)).logout(eq(refreshTokenRequest.refreshToken()));
    }

    @Test
    public void testConstructorWithUserService() {
        UserService mockUserService = mock(UserService.class);
        AuthController controller = new AuthController(mockUserService);
        assertNotNull(controller);
    }
}
