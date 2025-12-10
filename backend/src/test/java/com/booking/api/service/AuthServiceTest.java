package com.booking.api.service;

import com.booking.api.dto.request.LoginRequest;
import com.booking.api.dto.request.RegisterRequest;
import com.booking.api.dto.response.AuthResponse;
import com.booking.api.model.Business;
import com.booking.api.model.User;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.UserRepository;
import com.booking.api.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditService auditService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .passwordHash("$2a$10$hashedPassword")
                .role(User.UserRole.BUSINESS)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");
        registerRequest.setBusinessName("Test Business");
        registerRequest.setPhone("0123456789");
    }

    @Test
    void login_Success() {
        String accessToken = "jwt.access.token";
        String refreshToken = "jwt.refresh.token";
        org.springframework.security.core.userdetails.UserDetails userDetails =
            org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPasswordHash())
                .roles(testUser.getRole().name())
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(userDetails)).thenReturn(refreshToken);

        AuthResponse response = authService.login(loginRequest, null);

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());
        assertEquals(testUser.getRole(), response.getUser().getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(jwtService).generateToken(userDetails);
        verify(jwtService).generateRefreshToken(userDetails);
    }

    @Test
    void login_InvalidCredentials() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest, null));

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(loginRequest, null));

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_Success() {
        String accessToken = "jwt.access.token";
        String refreshToken = "jwt.refresh.token";
        String encodedPassword = "$2a$10$encodedPassword";
        org.springframework.security.core.userdetails.UserDetails userDetails =
            org.springframework.security.core.userdetails.User.builder()
                .username(registerRequest.getEmail())
                .password(encodedPassword)
                .roles("BUSINESS")
                .build();

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(businessRepository.existsBySlug(any())).thenReturn(false);
        when(businessRepository.save(any(Business.class))).thenAnswer(invocation -> {
            Business business = invocation.getArgument(0);
            business.setId(UUID.randomUUID());
            return business;
        });
        when(subscriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDetailsService.loadUserByUsername(registerRequest.getEmail())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(userDetails)).thenReturn(refreshToken);

        AuthResponse response = authService.register(registerRequest, null);

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
        assertEquals(registerRequest.getFirstName(), savedUser.getFirstName());
        assertEquals(registerRequest.getLastName(), savedUser.getLastName());
        assertEquals(encodedPassword, savedUser.getPasswordHash());

        ArgumentCaptor<Business> businessCaptor = ArgumentCaptor.forClass(Business.class);
        verify(businessRepository).save(businessCaptor.capture());

        Business savedBusiness = businessCaptor.getValue();
        assertEquals(registerRequest.getBusinessName(), savedBusiness.getBusinessName());
    }

    @Test
    void register_EmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest, null));

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any());
        verify(businessRepository, never()).save(any());
    }

    @Test
    void register_SlugAlreadyExists_GeneratesUnique() {
        String encodedPassword = "$2a$10$encodedPassword";
        org.springframework.security.core.userdetails.UserDetails userDetails =
            org.springframework.security.core.userdetails.User.builder()
                .username(registerRequest.getEmail())
                .password(encodedPassword)
                .roles("BUSINESS")
                .build();

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(businessRepository.existsBySlug(any()))
                .thenReturn(true, true, false); // First two slugs exist, third doesn't
        when(businessRepository.save(any(Business.class))).thenAnswer(invocation -> {
            Business business = invocation.getArgument(0);
            business.setId(UUID.randomUUID());
            return business;
        });
        when(subscriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        AuthResponse response = authService.register(registerRequest, null);

        assertNotNull(response);
        verify(businessRepository, atLeast(3)).existsBySlug(any());
        verify(businessRepository).save(any(Business.class));
    }

    @Test
    void register_PasswordIsEncoded() {
        String plainPassword = "mySecretPassword123";
        String encodedPassword = "$2a$10$encodedVersion";
        registerRequest.setPassword(plainPassword);
        org.springframework.security.core.userdetails.UserDetails userDetails =
            org.springframework.security.core.userdetails.User.builder()
                .username(registerRequest.getEmail())
                .password(encodedPassword)
                .roles("BUSINESS")
                .build();

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(businessRepository.existsBySlug(any())).thenReturn(false);
        when(businessRepository.save(any(Business.class))).thenAnswer(invocation -> {
            Business business = invocation.getArgument(0);
            business.setId(UUID.randomUUID());
            return business;
        });
        when(subscriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        authService.register(registerRequest, null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(encodedPassword, userCaptor.getValue().getPasswordHash());
    }

    @Test
    void register_BusinessSlugIsGenerated() {
        org.springframework.security.core.userdetails.UserDetails userDetails =
            org.springframework.security.core.userdetails.User.builder()
                .username(registerRequest.getEmail())
                .password("encoded")
                .roles("BUSINESS")
                .build();

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(businessRepository.existsBySlug(any())).thenReturn(false);
        when(businessRepository.save(any(Business.class))).thenAnswer(invocation -> {
            Business business = invocation.getArgument(0);
            business.setId(UUID.randomUUID());
            return business;
        });
        when(subscriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        authService.register(registerRequest, null);

        ArgumentCaptor<Business> businessCaptor = ArgumentCaptor.forClass(Business.class);
        verify(businessRepository).save(businessCaptor.capture());

        Business savedBusiness = businessCaptor.getValue();
        assertNotNull(savedBusiness.getSlug());
        assertTrue(savedBusiness.getSlug().startsWith("test-business"));
    }
}
