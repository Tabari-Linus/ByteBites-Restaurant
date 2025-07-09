package com.example.authservice.controller;

import com.example.authservice.dto.*;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe", null);
        JwtResponse jwtResponse = new JwtResponse("access-token", "refresh-token", "Bearer", 3600L, new UserInfo(UUID.randomUUID(), "test@example.com", "John", "Doe", Set.of("USER")));
        when(userService.register(any(RegisterRequest.class))).thenReturn(jwtResponse);
        ResponseEntity<SuccessMessage> response = authController.register(request);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertThat(response.getBody(), notNullValue());
        assertThat(response.getBody().message(), equalTo("User registered successfully"));
        verify(userService, atLeast(1)).register(request);
    }

    @Test
    public void testRegisterFailure() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe", null);
        RuntimeException exception = new RuntimeException("Registration failed");
        when(userService.register(any(RegisterRequest.class))).thenThrow(exception);
        assertThrows(RuntimeException.class, () -> authController.register(request));
        verify(userService, atLeast(1)).register(request);
    }

    @Test
    public void testLoginSuccess() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        LoginResponse loginResponse = new LoginResponse("access-token", "refresh-token", "Bearer");
        when(userService.login(any(LoginRequest.class))).thenReturn(loginResponse);
        ResponseEntity<LoginResponse> response = authController.login(request);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(loginResponse));
        verify(userService, atLeast(1)).login(request);
    }

    @Test
    public void testLoginFailure() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        RuntimeException exception = new RuntimeException("Login failed");
        when(userService.login(any(LoginRequest.class))).thenThrow(exception);
        assertThrows(RuntimeException.class, () -> authController.login(request));
        verify(userService, atLeast(1)).login(request);
    }

    @Test
    public void testRefreshTokenSuccess() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        JwtResponse jwtResponse = new JwtResponse("new-access-token", "new-refresh-token", "Bearer", 3600L, new UserInfo(UUID.randomUUID(), "test@example.com", "John", "Doe", Set.of("USER")));
        when(userService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(jwtResponse);
        ResponseEntity<JwtResponse> response = authController.refreshToken(request);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(jwtResponse));
        verify(userService, atLeast(1)).refreshToken(request);
    }

    @Test
    public void testRefreshTokenFailure() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        RuntimeException exception = new RuntimeException("Token refresh failed");
        when(userService.refreshToken(any(RefreshTokenRequest.class))).thenThrow(exception);
        assertThrows(RuntimeException.class, () -> authController.refreshToken(request));
        verify(userService, atLeast(1)).refreshToken(request);
    }

    @Test
    public void testGetCurrentUserSuccess() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UUID userUUID = UUID.fromString(userId);
        UserInfo userInfo = new UserInfo(userUUID, "test@example.com", "John", "Doe", Set.of("USER"));
        when(userService.getCurrentUser(any(UUID.class))).thenReturn(userInfo);
        ResponseEntity<UserInfo> response = authController.getCurrentUser(userId);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userInfo));
        verify(userService, atLeast(1)).getCurrentUser(userUUID);
    }

    @Test
    public void testGetCurrentUserFailure() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UUID userUUID = UUID.fromString(userId);
        RuntimeException exception = new RuntimeException("User not found");
        when(userService.getCurrentUser(any(UUID.class))).thenThrow(exception);
        assertThrows(RuntimeException.class, () -> authController.getCurrentUser(userId));
        verify(userService, atLeast(1)).getCurrentUser(userUUID);
    }

    @Test
    public void testGetCurrentUserInvalidUUID() {
        String invalidUserId = "invalid-uuid";
        assertThrows(IllegalArgumentException.class, () -> authController.getCurrentUser(invalidUserId));
        verify(userService, never()).getCurrentUser(any(UUID.class));
    }

    @Test
    public void testLogoutSuccess() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        doNothing().when(userService).logout(anyString());
        ResponseEntity<Void> response = authController.logout(request);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        verify(userService, atLeast(1)).logout(request.refreshToken());
    }

    @Test
    public void testLogoutFailure() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        RuntimeException exception = new RuntimeException("Logout failed");
        doThrow(exception).when(userService).logout(anyString());
        assertThrows(RuntimeException.class, () -> authController.logout(request));
        verify(userService, atLeast(1)).logout(request.refreshToken());
    }
}
