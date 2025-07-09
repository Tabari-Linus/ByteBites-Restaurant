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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    public void testRegisterWithCustomerRole() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe", null);
        Role customerRole = new Role(RoleName.ROLE_CUSTOMER);
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.addRole(customerRole);
        JwtResponse expectedResponse = new JwtResponse("accessToken", "refreshToken", "Bearer", 3600L, null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateTokenResponse(any(User.class))).thenReturn(expectedResponse);
        JwtResponse result = userService.register(request);
        assertThat(result, is(notNullValue()));
        assertThat(result.accessToken(), is("accessToken"));
        assertThat(result.refreshToken(), is("refreshToken"));
        assertThat(result.tokenType(), is("Bearer"));
        verify(userRepository, atLeast(1)).save(any(User.class));
        verify(jwtService, atLeast(1)).generateTokenResponse(any(User.class));
    }

    @Test
    public void testRegisterWithAdminRole() throws Exception {
        RegisterRequest request = new RegisterRequest("admin@example.com", "password123", "Admin", "User", RoleName.ROLE_ADMIN);
        Role adminRole = new Role(RoleName.ROLE_ADMIN);
        User user = new User("admin@example.com", "encodedPassword", "Admin", "User");
        user.addRole(adminRole);
        JwtResponse expectedResponse = new JwtResponse("accessToken", "refreshToken", "Bearer", 3600L, null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateTokenResponse(any(User.class))).thenReturn(expectedResponse);
        JwtResponse result = userService.register(request);
        assertThat(result, is(notNullValue()));
        assertThat(result.accessToken(), is("accessToken"));
        verify(userRepository, atLeast(1)).save(any(User.class));
        verify(jwtService, atLeast(1)).generateTokenResponse(any(User.class));
    }

    @Test
    public void testRegisterWithRestaurantOwnerRole() throws Exception {
        RegisterRequest request = new RegisterRequest("owner@example.com", "password123", "Owner", "User", RoleName.ROLE_RESTAURANT_OWNER);
        Role ownerRole = new Role(RoleName.ROLE_RESTAURANT_OWNER);
        User user = new User("owner@example.com", "encodedPassword", "Owner", "User");
        user.addRole(ownerRole);
        JwtResponse expectedResponse = new JwtResponse("accessToken", "refreshToken", "Bearer", 3600L, null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_RESTAURANT_OWNER)).thenReturn(Optional.of(ownerRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateTokenResponse(any(User.class))).thenReturn(expectedResponse);
        JwtResponse result = userService.register(request);
        assertThat(result, is(notNullValue()));
        assertThat(result.accessToken(), is("accessToken"));
        verify(userRepository, atLeast(1)).save(any(User.class));
        verify(jwtService, atLeast(1)).generateTokenResponse(any(User.class));
    }

    @Test
    public void testRegisterWithValidationExceptionForNullFirstName() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", null, "Doe", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is("First name is required."));
    }

    @Test
    public void testRegisterWithValidationExceptionForNullLastName() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", null, null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is("Last name is required."));
    }

    @Test
    public void testRegisterWithValidationExceptionForBothNullNames() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", null, null, null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is("First name is required. Last name is required."));
    }

    @Test
    public void testRegisterWithValidationExceptionForNumericFirstName() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "123", "Doe", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is("First name and last name cannot contain numbers."));
    }

    @Test
    public void testRegisterWithValidationExceptionForNumericLastName() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "456", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is("First name and last name cannot contain numbers."));
    }

    @Test
    public void testRegisterWithValidationExceptionForBothNumericNames() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "123", "456", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is("First name and last name cannot contain numbers."));
    }

    @Test
    public void testRegisterWithRoleNotFoundExceptionForAdmin() {
        RegisterRequest request = new RegisterRequest("admin@example.com", "password123", "Admin", "User", RoleName.ROLE_ADMIN);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.empty());
        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is("Enter a correct role name for admin"));
    }

    @Test
    public void testRegisterWithRoleNotFoundExceptionForOwner() {
        RegisterRequest request = new RegisterRequest("owner@example.com", "password123", "Owner", "User", RoleName.ROLE_RESTAURANT_OWNER);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_RESTAURANT_OWNER)).thenReturn(Optional.empty());
        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is("Enter a correct role name for owner"));
    }

    @Test
    public void testRegisterWithRoleNotFoundExceptionForCustomer() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe", null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.empty());
        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is("Default role not found"));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        Role customerRole = new Role(RoleName.ROLE_CUSTOMER);
        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.addRole(customerRole);
        JwtResponse jwtResponse = new JwtResponse("accessToken", "refreshToken", "Bearer", 3600L, null);
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateTokenResponse(any(User.class))).thenReturn(jwtResponse);
        LoginResponse result = userService.login(request);
        assertThat(result, is(notNullValue()));
        assertThat(result.accessToken(), is("accessToken"));
        assertThat(result.refreshToken(), is("refreshToken"));
        assertThat(result.tokenType(), is("Bearer"));
        verify(userRepository, atLeast(1)).save(any(User.class));
        verify(jwtService, atLeast(1)).generateTokenResponse(any(User.class));
    }

    @Test
    public void testLoginWithInvalidEmail() {
        LoginRequest request = new LoginRequest("invalid@example.com", "password123");
        when(userRepository.findByEmailWithRoles("invalid@example.com")).thenReturn(Optional.empty());
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.login(request);
        });
        assertThat(exception.getMessage(), is("Invalid email or password"));
    }

    @Test
    public void testLoginWithInvalidPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.login(request);
        });
        assertThat(exception.getMessage(), is("Invalid email or password"));
    }

    @Test
    public void testLoginWithDisabledAccount() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setEnabled(false);
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        AccountDisabledException exception = assertThrows(AccountDisabledException.class, () -> {
            userService.login(request);
        });
        assertThat(exception.getMessage(), is("User account is disabled"));
    }

    @Test
    public void testRefreshTokenSuccess() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("validRefreshToken");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        RefreshToken refreshToken = mock(RefreshToken.class);
        JwtResponse jwtResponse = new JwtResponse("newAccessToken", "newRefreshToken", "Bearer", 3600L, null);
        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(refreshToken));
        when(refreshToken.isExpired()).thenReturn(false);
        when(refreshToken.getUser()).thenReturn(user);
        when(jwtService.generateTokenResponse(user)).thenReturn(jwtResponse);
        JwtResponse result = userService.refreshToken(request);
        assertThat(result, is(notNullValue()));
        assertThat(result.accessToken(), is("newAccessToken"));
        assertThat(result.refreshToken(), is("newRefreshToken"));
        assertThat(result.tokenType(), is("Bearer"));
        verify(jwtService, atLeast(1)).generateTokenResponse(user);
    }

    @Test
    public void testRefreshTokenWithInvalidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalidToken");
        when(refreshTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> {
            userService.refreshToken(request);
        });
        assertThat(exception.getMessage(), is("Invalid refresh token"));
    }

    @Test
    public void testRefreshTokenWithExpiredToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("expiredToken");
        RefreshToken refreshToken = mock(RefreshToken.class);
        when(refreshTokenRepository.findByToken("expiredToken")).thenReturn(Optional.of(refreshToken));
        when(refreshToken.isExpired()).thenReturn(true);
        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
            userService.refreshToken(request);
        });
        assertThat(exception.getMessage(), is("Refresh token is expired"));
        verify(refreshTokenRepository, atLeast(1)).delete(refreshToken);
    }

    @Test
    public void testGetCurrentUserSuccess() throws Exception {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Role customerRole = new Role(RoleName.ROLE_CUSTOMER);
        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setId(userId);
        user.addRole(customerRole);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserInfo result = userService.getCurrentUser(userId);
        assertThat(result, is(notNullValue()));
        assertThat(result.id(), is(userId));
        assertThat(result.email(), is("test@example.com"));
        assertThat(result.firstName(), is("John"));
        assertThat(result.lastName(), is("Doe"));
        assertThat(result.roles().size(), is(1));
        assertThat(result.roles().contains("ROLE_CUSTOMER"), is(true));
    }

    @Test
    public void testGetCurrentUserNotFound() {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getCurrentUser(userId);
        });
        assertThat(exception.getCause().getClass(), is(UserNotFoundException.class));
        assertThat(exception.getCause().getMessage(), is("User not found"));
    }

    @Test
    public void testLogoutSuccess() {
        String refreshToken = "validRefreshToken";
        RefreshToken token = mock(RefreshToken.class);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(token));
        userService.logout(refreshToken);
        verify(refreshTokenRepository, atLeast(1)).delete(token);
    }

    @Test
    public void testLogoutWithInvalidToken() {
        String refreshToken = "invalidToken";
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());
        userService.logout(refreshToken);
        verify(refreshTokenRepository, atLeast(1)).findByToken(refreshToken);
    }

    @Test
    public void testRegisterWithMixedValidationErrors() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", null, "123", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertThat(exception.getMessage(), is("First name is required. First name and last name cannot contain numbers."));
    }

    @Test
    public void testGetCurrentUserWithMultipleRoles() throws Exception {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Role customerRole = new Role(RoleName.ROLE_CUSTOMER);
        Role adminRole = new Role(RoleName.ROLE_ADMIN);
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setId(userId);
        user.addRole(customerRole);
        user.addRole(adminRole);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserInfo result = userService.getCurrentUser(userId);
        assertThat(result, is(notNullValue()));
        assertThat(result.roles().size(), is(2));
        assertThat(result.roles().contains("ROLE_CUSTOMER"), is(true));
        assertThat(result.roles().contains("ROLE_ADMIN"), is(true));
    }
}
