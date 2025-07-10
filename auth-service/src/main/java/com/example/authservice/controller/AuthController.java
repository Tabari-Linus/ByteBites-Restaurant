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
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessMessage("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request for email: {}", request.email());
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Token refresh request");
        JwtResponse response = userService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(@RequestHeader("X-User-Id") String userId) {
        logger.info("Get current user request for ID: {}", userId);
        UserInfo userInfo = userService.getCurrentUser(UUID.fromString(userId));
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<Set<String>> getRoles(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(userService.getRoles(UUID.fromString(userId)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Logout request");
        userService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }
}