package com.example.authservice.controller;

import com.example.authservice.dto.*;
import com.example.authservice.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<SuccessMessage> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration request for email: {}", request.email());

        try {
            JwtResponse response = userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessMessage("User registered successfully"));
        } catch (RuntimeException e) {
            logger.error("Registration failed for email: {}, error: {}", request.email(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request for email: {}", request.email());

        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Login failed for email: {}, error: {}", request.email(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Token refresh request");

        try {
            JwtResponse response = userService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(@RequestHeader("X-User-Id") String userId) {
        logger.info("Get current user request for ID: {}", userId);

        try {
            UserInfo userInfo = userService.getCurrentUser(UUID.fromString(userId));
            return ResponseEntity.ok(userInfo);
        } catch (RuntimeException e) {
            logger.error("Get current user failed for ID: {}, error: {}", userId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Logout request");

        try {
            userService.logout(request.refreshToken());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Logout failed: {}", e.getMessage());
            throw e;
        }
    }
}