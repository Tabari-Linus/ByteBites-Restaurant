package com.example.authservice.service;

import com.example.authservice.dto.*;
import com.example.authservice.enums.RoleName;
import com.example.authservice.exception.*;
import com.example.authservice.model.RefreshToken;
import com.example.authservice.model.Role;
import com.example.authservice.model.User;
import com.example.authservice.repository.RefreshTokenRepository;
import com.example.authservice.repository.RoleRepository;
import com.example.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public JwtResponse register(RegisterRequest request) throws UserAlreadyExistsException, ValidationException {
        logger.info("Registering new user with email: {}", request.email());
        validateRegistrationRequest(request);
        User user = createUserWithDefaultRole(request);
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());
        return jwtService.generateTokenResponse(savedUser);
    }

    private void validateRegistrationRequest(RegisterRequest request) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        if (request.firstName() == null) {
            errors.append("First name is required. ");
        }
        if (request.lastName() == null) {
            errors.append("Last name is required. ");
        }
        if (request.firstName() != null && request.firstName().matches("[0-9]+") ||
                request.lastName() != null && request.lastName().matches("[0-9]+")) {
            errors.append("First name and last name cannot contain numbers. ");
        }

        if (errors.length() > 0) {
            throw new ValidationException(errors.toString().trim());
        }
    }


    private User createUserWithDefaultRole(RegisterRequest request) throws RoleNotFoundException {
        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName()
        );

        Role customerRole;
        if(request.role() != null && request.role() == RoleName.ROLE_ADMIN) {
            customerRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RoleNotFoundException("Enter a correct role name for admin"));
        } else if (request.role() != null && request.role() == RoleName.ROLE_RESTAURANT_OWNER)
        {
            customerRole = roleRepository.findByName(RoleName.ROLE_RESTAURANT_OWNER)
                    .orElseThrow(() -> new RoleNotFoundException("Enter a correct role name for owner"));
        } else {
            customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                    .orElseThrow(() -> new RoleNotFoundException("Default role not found"));
        }
        user.addRole(customerRole);
        return user;
    }

    public LoginResponse login(LoginRequest request) throws AccountDisabledException {
        logger.info("Attempting login for email: {}", request.email());
        User user = userRepository.findByEmailWithRoles(request.email())
                .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE));

        validateLoginAttempt(user, request.password());
        updateLastLogin(user);

        logger.info("User logged in successfully: {}", user.getId());
        JwtResponse loginData = jwtService.generateTokenResponse(user);
        return( new LoginResponse(
                loginData.accessToken(),
                loginData.refreshToken(),
                loginData.tokenType()
        ));


    }

    private void validateLoginAttempt(User user, String password) throws AccountDisabledException {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }
        if (!user.getEnabled()) {
            throw new AccountDisabledException("User account is disabled");
        }
    }

    private void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public JwtResponse refreshToken(RefreshTokenRequest request) throws InvalidTokenException, TokenExpiredException {
        logger.info("Attempting to refresh token");
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token is expired");
        }

        return jwtService.generateTokenResponse(refreshToken.getUser());
    }

    @Transactional(readOnly = true)
    public UserInfo getCurrentUser(UUID userId) throws UserNotFoundException {
        logger.info("Getting current user info for ID: {}", userId);
        User user = null;
        try {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }

        return new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream()
                        .map(role -> role.getName().toString())
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

    public void logout(String refreshToken) {
        logger.info("Logging out user");
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }
}