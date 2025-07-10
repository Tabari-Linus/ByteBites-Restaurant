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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    public void testRegisterShouldReturnCreatedStatusWithSuccessMessage() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe", null);
        ResponseEntity<SuccessMessage> response = authController.register(request);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().message(), is("User registered successfully"));
        verify(userService, atLeast(1)).register(request);
    }

    @Test
    public void testLoginShouldReturnOkStatusWithLoginResponse() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        LoginResponse expectedResponse = new LoginResponse("accessToken", "refreshToken", "Bearer");
        doReturn(expectedResponse).when(userService).login(any(LoginRequest.class));
        ResponseEntity<LoginResponse> response = authController.login(request);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(expectedResponse));
        verify(userService, atLeast(1)).login(request);
    }

    @Test
    public void testRefreshTokenShouldReturnOkStatusWithJwtResponse() {
        RefreshTokenRequest request = new RefreshTokenRequest("refreshToken123");
        JwtResponse expectedResponse = new JwtResponse("newAccessToken", "newRefreshToken", "Bearer", 3600L, null);
        doReturn(expectedResponse).when(userService).refreshToken(any(RefreshTokenRequest.class));
        ResponseEntity<JwtResponse> response = authController.refreshToken(request);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(expectedResponse));
        verify(userService, atLeast(1)).refreshToken(request);
    }

    @Test
    public void testGetCurrentUserShouldReturnOkStatusWithUserInfo() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUUID = UUID.fromString(userId);
        UserInfo expectedUserInfo = new UserInfo(userUUID, "test@example.com", "John", "Doe", Set.of("USER"));
        doReturn(expectedUserInfo).when(userService).getCurrentUser(any(UUID.class));
        ResponseEntity<UserInfo> response = authController.getCurrentUser(userId);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(expectedUserInfo));
        verify(userService, atLeast(1)).getCurrentUser(userUUID);
    }

    @Test
    public void testGetRolesShouldReturnOkStatusWithRoles() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUUID = UUID.fromString(userId);
        Set<String> expectedRoles = Set.of("USER", "ADMIN");
        doReturn(expectedRoles).when(userService).getRoles(any(UUID.class));
        ResponseEntity<Set<String>> response = authController.getRoles(userId);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(expectedRoles));
        verify(userService, atLeast(1)).getRoles(userUUID);
    }

    @Test
    public void testLogoutShouldReturnOkStatusWithVoidResponse() {
        RefreshTokenRequest request = new RefreshTokenRequest("refreshToken123");
        doNothing().when(userService).logout(anyString());
        ResponseEntity<Void> response = authController.logout(request);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        verify(userService, atLeast(1)).logout(request.refreshToken());
    }

    @Test
    public void testAuthControllerConstructorShouldCreateInstanceSuccessfully() {
        AuthController controller = new AuthController(userService);
        assertNotNull(controller);
    }
}
