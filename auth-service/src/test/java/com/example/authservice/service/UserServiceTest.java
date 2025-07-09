package com.example.authservice.service;

import com.example.authservice.dto.JwtResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.LoginResponse;
import com.example.authservice.dto.RefreshTokenRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.UserInfo;
import com.example.authservice.enums.RoleName;
import com.example.authservice.exception.AccountDisabledException;
import com.example.authservice.exception.InvalidTokenException;
import com.example.authservice.exception.RoleNotFoundException;
import com.example.authservice.exception.TokenExpiredException;
import com.example.authservice.exception.UserNotFoundException;
import com.example.authservice.exception.ValidationException;
import com.example.authservice.model.RefreshToken;
import com.example.authservice.model.Role;
import com.example.authservice.model.User;
import com.example.authservice.repository.RefreshTokenRepository;
import com.example.authservice.repository.RoleRepository;
import com.example.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Timeout(10)
public class UserServiceTest {

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

    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, roleRepository, refreshTokenRepository, passwordEncoder, jwtService);
    }

    @Test
    public void registerShouldReturnJwtResponseWhenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe", null);
        Role customerRole = new Role(RoleName.ROLE_CUSTOMER);
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.addRole(customerRole);
        User savedUser = new User("test@example.com", "encodedPassword", "John", "Doe");
        savedUser.setId(UUID.randomUUID());
        JwtResponse jwtResponse = new JwtResponse("accessToken", "refreshToken", "Bearer", 3600L, null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateTokenResponse(savedUser)).thenReturn(jwtResponse);
        JwtResponse result = userService.register(request);
        assertThat(result, is(notNullValue()));
        assertThat(result.accessToken(), is(equalTo("accessToken")));
        assertThat(result.refreshToken(), is(equalTo("refreshToken")));
        verify(userRepository, atLeast(1)).save(any(User.class));
        verify(jwtService, atLeast(1)).generateTokenResponse(savedUser);
    }

    @Test
    public void registerShouldReturnJwtResponseWhenAdminRole() throws Exception {
        RegisterRequest request = new RegisterRequest("admin@example.com", "password123", "Admin", "User", RoleName.ROLE_ADMIN);
        Role adminRole = new Role(RoleName.ROLE_ADMIN);
        User user = new User("admin@example.com", "encodedPassword", "Admin", "User");
        user.addRole(adminRole);
        User savedUser = new User("admin@example.com", "encodedPassword", "Admin", "User");
        savedUser.setId(UUID.randomUUID());
        JwtResponse jwtResponse = new JwtResponse("accessToken", "refreshToken", "Bearer", 3600L, null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateTokenResponse(savedUser)).thenReturn(jwtResponse);
        JwtResponse result = userService.register(request);
        assertThat(result, is(notNullValue()));
        assertThat(result.accessToken(), is(equalTo("accessToken")));
        verify(roleRepository, atLeast(1)).findByName(RoleName.ROLE_ADMIN);
    }

    @Test
    public void registerShouldReturnJwtResponseWhenRestaurantOwnerRole() throws Exception {
        RegisterRequest request = new RegisterRequest("owner@example.com", "password123", "Owner", "User", RoleName.ROLE_RESTAURANT_OWNER);
        Role ownerRole = new Role(RoleName.ROLE_RESTAURANT_OWNER);
        User user = new User("owner@example.com", "encodedPassword", "Owner", "User");
        user.addRole(ownerRole);
        User savedUser = new User("owner@example.com", "encodedPassword", "Owner", "User");
        savedUser.setId(UUID.randomUUID());
        JwtResponse jwtResponse = new JwtResponse("accessToken", "refreshToken", "Bearer", 3600L, null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_RESTAURANT_OWNER)).thenReturn(Optional.of(ownerRole));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateTokenResponse(savedUser)).thenReturn(jwtResponse);
        JwtResponse result = userService.register(request);
        assertThat(result, is(notNullValue()));
        assertThat(result.accessToken(), is(equalTo("accessToken")));
        verify(roleRepository, atLeast(1)).findByName(RoleName.ROLE_RESTAURANT_OWNER);
    }

    @Test
    public void registerShouldThrowValidationExceptionWhenEmailIsNull() {
        RegisterRequest request = new RegisterRequest(null, "password123", "John", "Doe", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Email cannot be null or empty")));
    }

    @Test
    public void registerShouldThrowValidationExceptionWhenEmailIsEmpty() {
        RegisterRequest request = new RegisterRequest("", "password123", "John", "Doe", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Email cannot be null or empty")));
    }

    @Test
    public void registerShouldThrowValidationExceptionWhenPasswordIsNull() {
        RegisterRequest request = new RegisterRequest("test@example.com", null, "John", "Doe", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Password must be at least 8 characters long")));
    }

    @Test
    public void registerShouldThrowValidationExceptionWhenPasswordIsTooShort() {
        RegisterRequest request = new RegisterRequest("test@example.com", "short", "John", "Doe", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Password must be at least 8 characters long")));
    }

    @Test
    public void registerShouldThrowValidationExceptionWhenFirstNameIsNull() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", null, "Doe", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is(equalTo("First name cannot be null or empty")));
    }

    @Test
    public void registerShouldThrowValidationExceptionWhenFirstNameIsEmpty() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "", "Doe", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is(equalTo("First name cannot be null or empty")));
    }

    @Test
    public void registerShouldThrowValidationExceptionWhenLastNameIsNull() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", null, null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Last name cannot be null or empty")));
    }

    @Test
    public void registerShouldThrowValidationExceptionWhenLastNameIsEmpty() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Last name cannot be null or empty")));
    }

    @Test
    public void registerShouldThrowRoleNotFoundExceptionWhenCustomerRoleNotFound() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe", null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.empty());
        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Default role not found")));
    }

    @Test
    public void registerShouldThrowRoleNotFoundExceptionWhenAdminRoleNotFound() {
        RegisterRequest request = new RegisterRequest("admin@example.com", "password123", "Admin", "User", RoleName.ROLE_ADMIN);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.empty());
        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Enter a correct role name for admin")));
    }

    @Test
    public void registerShouldThrowRoleNotFoundExceptionWhenRestaurantOwnerRoleNotFound() {
        RegisterRequest request = new RegisterRequest("owner@example.com", "password123", "Owner", "User", RoleName.ROLE_RESTAURANT_OWNER);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_RESTAURANT_OWNER)).thenReturn(Optional.empty());
        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Enter a correct role name for owner")));
    }

    @Test
    public void loginShouldReturnLoginResponseWhenValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setId(UUID.randomUUID());
        user.setEnabled(true);
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(RoleName.ROLE_CUSTOMER));
        user.setRoles(roles);
        JwtResponse jwtResponse = new JwtResponse("accessToken", "refreshToken", "Bearer", 3600L, null);
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateTokenResponse(user)).thenReturn(jwtResponse);
        LoginResponse result = userService.login(request);
        assertThat(result, is(notNullValue()));
        assertThat(result.accessToken(), is(equalTo("accessToken")));
        assertThat(result.refreshToken(), is(equalTo("refreshToken")));
        assertThat(result.tokenType(), is(equalTo("Bearer")));
        verify(userRepository, atLeast(1)).save(any(User.class));
    }

    @Test
    public void loginShouldThrowBadCredentialsExceptionWhenUserNotFound() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.empty());
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.login(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Invalid email or password")));
    }

    @Test
    public void loginShouldThrowBadCredentialsExceptionWhenPasswordInvalid() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setEnabled(true);
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.login(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Invalid email or password")));
    }

    @Test
    public void loginShouldThrowAccountDisabledExceptionWhenUserDisabled() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setEnabled(false);
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        AccountDisabledException exception = assertThrows(AccountDisabledException.class, () -> {
            userService.login(request);
        });
        assertThat(exception.getMessage(), is(equalTo("User account is disabled")));
    }

    @Test
    public void refreshTokenShouldReturnJwtResponseWhenValidToken() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("validRefreshToken");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setId(UUID.randomUUID());
        RefreshToken refreshToken = new RefreshToken("validRefreshToken", user, LocalDateTime.now().plusHours(1));
        JwtResponse jwtResponse = new JwtResponse("newAccessToken", "newRefreshToken", "Bearer", 3600L, null);
        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateTokenResponse(user)).thenReturn(jwtResponse);
        JwtResponse result = userService.refreshToken(request);
        assertThat(result, is(notNullValue()));
        assertThat(result.accessToken(), is(equalTo("newAccessToken")));
        assertThat(result.refreshToken(), is(equalTo("newRefreshToken")));
    }

    @Test
    public void refreshTokenShouldThrowInvalidTokenExceptionWhenTokenNotFound() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalidToken");
        when(refreshTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> {
            userService.refreshToken(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Invalid refresh token")));
    }

    @Test
    public void refreshTokenShouldThrowTokenExpiredExceptionWhenTokenExpired() {
        RefreshTokenRequest request = new RefreshTokenRequest("expiredToken");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        RefreshToken refreshToken = new RefreshToken("expiredToken", user, LocalDateTime.now().minusHours(1));
        when(refreshTokenRepository.findByToken("expiredToken")).thenReturn(Optional.of(refreshToken));
        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
            userService.refreshToken(request);
        });
        assertThat(exception.getMessage(), is(equalTo("Refresh token is expired")));
        verify(refreshTokenRepository, atLeast(1)).delete(refreshToken);
    }

    @Test
    public void getCurrentUserShouldReturnUserInfoWhenUserExists() throws Exception {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setId(userId);
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(RoleName.ROLE_CUSTOMER));
        user.setRoles(roles);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserInfo result = userService.getCurrentUser(userId);
        assertThat(result, is(notNullValue()));
        assertThat(result.id(), is(equalTo(userId)));
        assertThat(result.email(), is(equalTo("test@example.com")));
        assertThat(result.firstName(), is(equalTo("John")));
        assertThat(result.lastName(), is(equalTo("Doe")));
        assertThat(result.roles().size(), is(equalTo(1)));
        assertThat(result.roles().iterator().next(), is(equalTo("ROLE_CUSTOMER")));
    }

    @Test
    public void getCurrentUserShouldThrowUserNotFoundExceptionWhenUserNotExists() {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getCurrentUser(userId);
        });
        assertThat(exception.getMessage(), is(equalTo("User not found")));
    }

    @Test
    public void logoutShouldDeleteRefreshTokenWhenTokenExists() {
        String refreshToken = "validRefreshToken";
        RefreshToken token = mock(RefreshToken.class);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(token));
        userService.logout(refreshToken);
        verify(refreshTokenRepository, atLeast(1)).delete(token);
    }

    @Test
    public void logoutShouldNotDeleteWhenTokenNotExists() {
        String refreshToken = "invalidRefreshToken";
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());
        userService.logout(refreshToken);
        verify(refreshTokenRepository, atLeast(1)).findByToken(refreshToken);
    }
}
