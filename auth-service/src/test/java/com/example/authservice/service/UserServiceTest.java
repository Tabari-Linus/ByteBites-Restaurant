package com.example.authservice.service;

import com.example.authservice.dto.JwtResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.enums.RoleName;
import com.example.authservice.model.User;
import com.example.authservice.model.Role;
import java.util.Optional;
import com.example.authservice.repository.RefreshTokenRepository;
import com.example.authservice.repository.RoleRepository;
import com.example.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterNewUser() {
        var request = new RegisterRequest("test@example.com", "password", "Test", "User");
        var customerRole = new Role();
        customerRole.setName(RoleName.ROLE_CUSTOMER);

        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateTokenResponse(any(User.class)))
                .thenReturn(new JwtResponse("jwt-token", "refresh-token", "Bearer", 3600L, null));

        JwtResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void shouldAuthenticateUser() {
        
        var request = new LoginRequest("test@example.com", "password123");
        var hashedPassword = "hashed_password123";

        var user = new User();
        user.setEmail(request.email());
        user.setPassword(hashedPassword);
        user.setEnabled(true);

        when(userRepository.findByEmailWithRoles(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), hashedPassword)).thenReturn(true);
        when(jwtService.generateTokenResponse(user))
                .thenReturn(new JwtResponse("jwt-token-2", "refresh-token-2", "Bearer", 3600L, null));

        
        JwtResponse response = userService.login(request);

        
        assertNotNull(response);
        assertEquals("jwt-token-2", response.accessToken());
        verify(passwordEncoder).matches(request.password(), hashedPassword);
        verify(jwtService).generateTokenResponse(user);
    }
}