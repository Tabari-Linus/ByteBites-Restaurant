package com.example.authservice.controller;

import com.example.authservice.dto.*;
import com.example.authservice.exception.GlobalExceptionHandler;
import com.example.authservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler()) 
                .build();
        objectMapper = new ObjectMapper();
    }


    @Test
    void shouldRegisterUser() throws Exception {
        
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "password123",
                "John",
                "Doe"
        );

        UserInfo userInfo = new UserInfo(
                UUID.randomUUID(),
                "test@example.com",
                "John",
                "Doe",
                Set.of("ROLE_CUSTOMER")
        );

        JwtResponse expectedResponse = new JwtResponse(
                "access.token",
                "refresh.token",
                "Bearer",
                3600L,
                userInfo
        );

        when(userService.register(request)).thenReturn(expectedResponse);

        
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("access.token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(userService).register(request);
    }

    @Test
    void shouldLoginUser() throws Exception {
        
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        JwtResponse expectedResponse = new JwtResponse(
                "access.token",
                "refresh.token",
                "Bearer",
                3600L,
                null
        );

        when(userService.login(request)).thenReturn(expectedResponse);

        
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.token"));

        verify(userService).login(request);
    }

    @Test
    void shouldRefreshToken() throws Exception {
        
        RefreshTokenRequest request = new RefreshTokenRequest("refresh.token");
        JwtResponse expectedResponse = new JwtResponse(
                "new.access.token",
                "new.refresh.token",
                "Bearer",
                3600L,
                null
        );

        when(userService.refreshToken(request)).thenReturn(expectedResponse);

        
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("new.refresh.token"));

        verify(userService).refreshToken(request);
    }

    @Test
    void shouldGetCurrentUser() throws Exception {
        
        UUID userId = UUID.randomUUID();
        UserInfo expectedUser = new UserInfo(
                userId,
                "test@example.com",
                "John",
                "Doe",
                Set.of("ROLE_CUSTOMER")
        );

        when(userService.getCurrentUser(userId)).thenReturn(expectedUser);

        
        mockMvc.perform(get("/auth/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService).getCurrentUser(userId);
    }

    @Test
    void shouldLogoutUser() throws Exception {
        
        RefreshTokenRequest request = new RefreshTokenRequest("refresh.token");
        doNothing().when(userService).logout(request.refreshToken());

        
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).logout(request.refreshToken());
    }

    @Test
    void shouldHandleRegistrationError() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "existing@example.com",
                "password123",
                "John",
                "Doe"
        );

        when(userService.register(any()))
                .thenThrow(new RuntimeException("User already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User already exists"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldHandleLoginError() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        when(userService.login(any()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.status").value(401));
    }
}