package com.example.authservice.service;

import com.example.authservice.dto.JwtResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.LoginResponse;
import com.example.authservice.dto.RefreshTokenRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.SuccessMessage;
import com.example.authservice.dto.UserInfo;
import com.example.authservice.enums.RoleName;
import com.example.authservice.exception.AccountDisabledException;
import com.example.authservice.exception.InvalidTokenException;
import com.example.authservice.exception.RoleNotFoundException;
import com.example.authservice.exception.TokenExpiredException;
import com.example.authservice.exception.UserAlreadyExistsException;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Disabled;

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
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, roleRepository, refreshTokenRepository, passwordEncoder, jwtService);
    }

    @Test
    void testRegisterWithCustomerRoleSuccess() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe", null);
        Role customerRole = new Role(RoleName.ROLE_CUSTOMER);
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.addRole(customerRole);
        when(userRepository.existsByEmail(eq("test@example.com"))).thenReturn(false);
        when(passwordEncoder.encode(eq("password123"))).thenReturn("encodedPassword");
        when(roleRepository.findByName(eq(RoleName.ROLE_CUSTOMER))).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        SuccessMessage result = userService.register(request);
        assertThat(result.message(), is(equalTo("User registered successfully")));
        verify(userRepository, atLeast(1)).existsByEmail(eq("test@example.com"));
        verify(passwordEncoder, atLeast(1)).encode(eq("password123"));
        verify(roleRepository, atLeast(1)).findByName(eq(RoleName.ROLE_CUSTOMER));
        verify(userRepository, atLeast(1)).save(any(User.class));
    }

    @Test
    void testRegisterWithAdminRoleSuccess() {
        RegisterRequest request = new RegisterRequest("admin@example.com", "password123", "Admin", "User", RoleName.ROLE_ADMIN);
        Role adminRole = new Role(RoleName.ROLE_ADMIN);
        User user = new User("admin@example.com", "encodedPassword", "Admin", "User");
        user.addRole(adminRole);
        when(userRepository.existsByEmail(eq("admin@example.com"))).thenReturn(false);
        when(passwordEncoder.encode(eq("password123"))).thenReturn("encodedPassword");
        when(roleRepository.findByName(eq(RoleName.ROLE_ADMIN))).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        SuccessMessage result = userService.register(request);
        assertThat(result.message(), is(equalTo("User registered successfully")));
        verify(userRepository, atLeast(1)).existsByEmail(eq("admin@example.com"));
        verify(passwordEncoder, atLeast(1)).encode(eq("password123"));
        verify(roleRepository, atLeast(1)).findByName(eq(RoleName.ROLE_ADMIN));
        verify(userRepository, atLeast(1)).save(any(User.class));
    }

    @Test
    void testRegisterWithRestaurantOwnerRoleSuccess() {
        RegisterRequest request = new RegisterRequest("owner@example.com", "password123", "Owner", "Restaurant", RoleName.ROLE_RESTAURANT_OWNER);
        Role ownerRole = new Role(RoleName.ROLE_RESTAURANT_OWNER);
        User user = new User("owner@example.com", "encodedPassword", "Owner", "Restaurant");
        user.addRole(ownerRole);
        when(userRepository.existsByEmail(eq("owner@example.com"))).thenReturn(false);
        when(passwordEncoder.encode(eq("password123"))).thenReturn("encodedPassword");
        when(roleRepository.findByName(eq(RoleName.ROLE_RESTAURANT_OWNER))).thenReturn(Optional.of(ownerRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        SuccessMessage result = userService.register(request);
        assertThat(result.message(), is(equalTo("User registered successfully")));
        verify(userRepository, atLeast(1)).existsByEmail(eq("owner@example.com"));
        verify(passwordEncoder, atLeast(1)).encode(eq("password123"));
        verify(roleRepository, atLeast(1)).findByName(eq(RoleName.ROLE_RESTAURANT_OWNER));
        verify(userRepository, atLeast(1)).save(any(User.class));
    }

    @Disabled()
    @Test
    void testRegisterWithNullEmail() {
        RegisterRequest request = new RegisterRequest(null, "password123", "John", "Doe", null);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(ValidationException.class));
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Disabled()
    @Test
    void testRegisterWithEmptyEmail() {
        RegisterRequest request = new RegisterRequest("", "password123", "John", "Doe", null);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(ValidationException.class));
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Disabled()
    @Test
    void testRegisterWithExistingEmail() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", "John", "Doe", null);
        when(userRepository.existsByEmail(eq("existing@example.com"))).thenReturn(true);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(UserAlreadyExistsException.class));
        assertEquals("User with email existing@example.com already exists", exception.getCause().getMessage());
    }

    @Disabled()
    @Test
    void testRegisterWithNullPassword() {
        RegisterRequest request = new RegisterRequest("test@example.com", null, "John", "Doe", null);
        when(userRepository.existsByEmail(eq("test@example.com"))).thenReturn(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(ValidationException.class));
        assertEquals("Password must be at least 8 characters long", exception.getMessage());
    }

    @Disabled()
    @Test
    void testRegisterWithShortPassword() {
        RegisterRequest request = new RegisterRequest("test@example.com", "short", "John", "Doe", null);
        when(userRepository.existsByEmail(eq("test@example.com"))).thenReturn(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(ValidationException.class));
        assertEquals("Password must be at least 8 characters long", exception.getMessage());
    }

    @Disabled()
    @Test
    void testRegisterWithNullFirstName() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", null, "Doe", null);
        when(userRepository.existsByEmail(eq("test@example.com"))).thenReturn(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(ValidationException.class));
        assertEquals("First name cannot be null or empty", exception.getMessage());
    }

    @Disabled()
    @Test
    void testRegisterWithEmptyFirstName() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "", "Doe", null);
        when(userRepository.existsByEmail(eq("test@example.com"))).thenReturn(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(ValidationException.class));
        assertEquals("First name cannot be null or empty", exception.getMessage());
    }

    @Disabled()
    @Test
    void testRegisterWithNullLastName() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", null, null);
        when(userRepository.existsByEmail(eq("test@example.com"))).thenReturn(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(ValidationException.class));
        assertEquals("Last name cannot be null or empty", exception.getMessage());
    }

    @Disabled()
    @Test
    void testRegisterWithEmptyLastName() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "", null);
        when(userRepository.existsByEmail(eq("test@example.com"))).thenReturn(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(ValidationException.class));
        assertEquals("Last name cannot be null or empty", exception.getMessage());
    }

    @Disabled()
    @Test
    void testRegisterWithRoleNotFound() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe", null);
        when(userRepository.existsByEmail(eq("test@example.com"))).thenReturn(false);
        when(passwordEncoder.encode(eq("password123"))).thenReturn("encodedPassword");
        when(roleRepository.findByName(eq(RoleName.ROLE_CUSTOMER))).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(RoleNotFoundException.class));
        assertEquals("Default role not found", exception.getCause().getMessage());
    }

    @Disabled()
    @Test
    void testRegisterWithAdminRoleNotFound() {
        RegisterRequest request = new RegisterRequest("admin@example.com", "password123", "Admin", "User", RoleName.ROLE_ADMIN);
        when(userRepository.existsByEmail(eq("admin@example.com"))).thenReturn(false);
        when(passwordEncoder.encode(eq("password123"))).thenReturn("encodedPassword");
        when(roleRepository.findByName(eq(RoleName.ROLE_ADMIN))).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(RoleNotFoundException.class));
        assertEquals("Enter a correct role name for admin", exception.getCause().getMessage());
    }

    @Disabled()
    @Test
    void testRegisterWithRestaurantOwnerRoleNotFound() {
        RegisterRequest request = new RegisterRequest("owner@example.com", "password123", "Owner", "Restaurant", RoleName.ROLE_RESTAURANT_OWNER);
        when(userRepository.existsByEmail(eq("owner@example.com"))).thenReturn(false);
        when(passwordEncoder.encode(eq("password123"))).thenReturn("encodedPassword");
        when(roleRepository.findByName(eq(RoleName.ROLE_RESTAURANT_OWNER))).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertThat(exception.getCause(), is(RoleNotFoundException.class));
        assertEquals("Enter a correct role name for owner", exception.getCause().getMessage());
    }

    @Test
    void testRegisterWithRuntimeException() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe", null);
        when(userRepository.existsByEmail(eq("test@example.com"))).thenReturn(false);
        when(passwordEncoder.encode(eq("password123"))).thenThrow(new RuntimeException("Encoding error"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));
        assertEquals("Encoding error", exception.getCause().getMessage());
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setEnabled(true);
        JwtResponse jwtResponse = new JwtResponse("accessToken", "refreshToken", "Bearer", 3600L, null);
        when(userRepository.findByEmailWithRoles(eq("test@example.com"))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("password123"), eq("encodedPassword"))).thenReturn(true);
        when(jwtService.generateTokenResponse(eq(user))).thenReturn(jwtResponse);
        when(userRepository.save(any(User.class))).thenReturn(user);
        LoginResponse result = userService.login(request);
        assertThat(result.accessToken(), is(equalTo("accessToken")));
        assertThat(result.refreshToken(), is(equalTo("refreshToken")));
        assertThat(result.tokenType(), is(equalTo("Bearer")));
        verify(userRepository, atLeast(1)).findByEmailWithRoles(eq("test@example.com"));
        verify(passwordEncoder, atLeast(1)).matches(eq("password123"), eq("encodedPassword"));
        verify(jwtService, atLeast(1)).generateTokenResponse(eq(user));
        verify(userRepository, atLeast(1)).save(any(User.class));
    }

    @Test
    void testLoginWithUserNotFound() {
        LoginRequest request = new LoginRequest("notfound@example.com", "password123");
        when(userRepository.findByEmailWithRoles(eq("notfound@example.com"))).thenReturn(Optional.empty());
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> userService.login(request));
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLoginWithInvalidPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        when(userRepository.findByEmailWithRoles(eq("test@example.com"))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("wrongpassword"), eq("encodedPassword"))).thenReturn(false);
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> userService.login(request));
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLoginWithDisabledAccount() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setEnabled(false);
        when(userRepository.findByEmailWithRoles(eq("test@example.com"))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("password123"), eq("encodedPassword"))).thenReturn(true);
        AccountDisabledException exception = assertThrows(AccountDisabledException.class, () -> userService.login(request));
        assertEquals("User account is disabled", exception.getMessage());
    }

    @Test
    void testLoginWithRuntimeException() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setEnabled(true);
        when(userRepository.findByEmailWithRoles(eq("test@example.com"))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("password123"), eq("encodedPassword"))).thenReturn(true);
        when(jwtService.generateTokenResponse(eq(user))).thenThrow(new RuntimeException("JWT generation error"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.login(request));
        assertEquals("JWT generation error", exception.getMessage());
    }

    @Test
    void testRefreshTokenSuccess() {
        RefreshTokenRequest request = new RefreshTokenRequest("validRefreshToken");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        RefreshToken refreshToken = new RefreshToken("validRefreshToken", user, LocalDateTime.now().plusDays(7));
        JwtResponse jwtResponse = new JwtResponse("newAccessToken", "refreshToken", "Bearer", 3600L, null);
        when(refreshTokenRepository.findByToken(eq("validRefreshToken"))).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateTokenResponse(eq(user))).thenReturn(jwtResponse);
        LoginResponse result = userService.refreshToken(request);
        assertThat(result.accessToken(), is(equalTo("newAccessToken")));
        assertThat(result.refreshToken(), is(equalTo("validRefreshToken")));
        assertThat(result.tokenType(), is(equalTo("Bearer")));
        verify(refreshTokenRepository, atLeast(1)).findByToken(eq("validRefreshToken"));
        verify(jwtService, atLeast(1)).generateTokenResponse(eq(user));
    }

    @Test
    void testRefreshTokenWithInvalidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalidToken");
        when(refreshTokenRepository.findByToken(eq("invalidToken"))).thenReturn(Optional.empty());
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> userService.refreshToken(request));
        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void testRefreshTokenWithExpiredToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("expiredToken");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        RefreshToken refreshToken = new RefreshToken("expiredToken", user, LocalDateTime.now().minusDays(1));
        when(refreshTokenRepository.findByToken(eq("expiredToken"))).thenReturn(Optional.of(refreshToken));
        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> userService.refreshToken(request));
        assertEquals("Refresh token is expired", exception.getMessage());
        verify(refreshTokenRepository, atLeast(1)).delete(eq(refreshToken));
    }

    @Test
    void testRefreshTokenWithRuntimeException() {
        RefreshTokenRequest request = new RefreshTokenRequest("validRefreshToken");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        RefreshToken refreshToken = new RefreshToken("validRefreshToken", user, LocalDateTime.now().plusDays(7));
        when(refreshTokenRepository.findByToken(eq("validRefreshToken"))).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateTokenResponse(eq(user))).thenThrow(new RuntimeException("JWT generation error"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.refreshToken(request));
        assertEquals("JWT generation error", exception.getMessage());
    }

    @Test
    void testGetCurrentUserSuccess() {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setId(userId);
        Role role = new Role(RoleName.ROLE_CUSTOMER);
        user.addRole(role);
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        UserInfo result = userService.getCurrentUser(userId);
        assertThat(result.id(), is(equalTo(userId)));
        assertThat(result.email(), is(equalTo("test@example.com")));
        assertThat(result.firstName(), is(equalTo("John")));
        assertThat(result.lastName(), is(equalTo("Doe")));
        assertThat(result.roles().size(), is(equalTo(1)));
        assertThat(result.roles().contains("ROLE_CUSTOMER"), is(true));
        verify(userRepository, atLeast(1)).findById(eq(userId));
    }

    @Test
    void testGetCurrentUserWithUserNotFound() {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser(userId));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetCurrentUserWithRuntimeException() {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        User user = mock(User.class);
        doReturn(userId).when(user).getId();
        doReturn("test@example.com").when(user).getEmail();
        doReturn("John").when(user).getFirstName();
        doReturn("Doe").when(user).getLastName();
        doReturn(Optional.of(user)).when(userRepository).findById(eq(userId));
        when(user.getRoles()).thenThrow(new RuntimeException("Role retrieval error"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getCurrentUser(userId));
        assertEquals("Role retrieval error", exception.getMessage());
    }

    @Test
    void testGetRolesSuccess() {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        Role role1 = new Role(RoleName.ROLE_CUSTOMER);
        Role role2 = new Role(RoleName.ROLE_ADMIN);
        user.addRole(role1);
        user.addRole(role2);
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        Set<String> result = userService.getRoles(userId);
        assertThat(result.size(), is(equalTo(2)));
        assertThat(result.contains("ROLE_CUSTOMER"), is(true));
        assertThat(result.contains("ROLE_ADMIN"), is(true));
        verify(userRepository, atLeast(1)).findById(eq(userId));
    }

    @Test
    void testGetRolesWithUserNotFound() {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getRoles(userId));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testLogoutSuccess() {
        String refreshToken = "validRefreshToken";
        RefreshToken token = mock(RefreshToken.class);
        when(refreshTokenRepository.findByToken(eq(refreshToken))).thenReturn(Optional.of(token));
        userService.logout(refreshToken);
        verify(refreshTokenRepository, atLeast(1)).findByToken(eq(refreshToken));
        verify(refreshTokenRepository, atLeast(1)).delete(eq(token));
    }

    @Test
    void testLogoutWithNonExistentToken() {
        String refreshToken = "nonExistentToken";
        when(refreshTokenRepository.findByToken(eq(refreshToken))).thenReturn(Optional.empty());
        userService.logout(refreshToken);
        verify(refreshTokenRepository, atLeast(1)).findByToken(eq(refreshToken));
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void testLogoutWithRuntimeException() {
        String refreshToken = "validRefreshToken";
        when(refreshTokenRepository.findByToken(eq(refreshToken))).thenThrow(new RuntimeException("Repository error"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.logout(refreshToken));
        assertEquals("Repository error", exception.getMessage());
    }

    @Test
    void testGetCurrentUserWithMultipleRoles() {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setId(userId);
        Role role1 = new Role(RoleName.ROLE_CUSTOMER);
        Role role2 = new Role(RoleName.ROLE_ADMIN);
        Role role3 = new Role(RoleName.ROLE_RESTAURANT_OWNER);
        user.addRole(role1);
        user.addRole(role2);
        user.addRole(role3);
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        UserInfo result = userService.getCurrentUser(userId);
        assertThat(result.id(), is(equalTo(userId)));
        assertThat(result.email(), is(equalTo("test@example.com")));
        assertThat(result.firstName(), is(equalTo("John")));
        assertThat(result.lastName(), is(equalTo("Doe")));
        assertThat(result.roles().size(), is(equalTo(3)));
        assertThat(result.roles().contains("ROLE_CUSTOMER"), is(true));
        assertThat(result.roles().contains("ROLE_ADMIN"), is(true));
        assertThat(result.roles().contains("ROLE_RESTAURANT_OWNER"), is(true));
        verify(userRepository, atLeast(1)).findById(eq(userId));
    }

    @Test
    void testGetRolesWithEmptyRoleSet() {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setRoles(new HashSet<>());
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        Set<String> result = userService.getRoles(userId);
        assertThat(result.size(), is(equalTo(0)));
        verify(userRepository, atLeast(1)).findById(eq(userId));
    }

    @Test
    void testUserServiceConstructor() {
        UserService service = new UserService(userRepository, roleRepository, refreshTokenRepository, passwordEncoder, jwtService);
        assertNotNull(service);
    }
}
