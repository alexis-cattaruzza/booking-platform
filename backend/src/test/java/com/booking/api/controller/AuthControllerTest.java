package com.booking.api.controller;

import com.booking.api.dto.request.LoginRequest;
import com.booking.api.dto.request.RegisterRequest;
import com.booking.api.dto.response.AuthResponse;
import com.booking.api.model.User;
import com.booking.api.service.AuthService;
import com.booking.api.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour AuthController
 * Test des endpoints d'authentification : register, login, refresh, logout
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) 
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123")
                .firstName("John")
                .lastName("Doe")
                .phone("+33612345678")
                .businessName("Test Salon")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.UserRole.BUSINESS)
                .build();

        authResponse = AuthResponse.builder()
                .accessToken("access-token-jwt")
                .refreshToken("refresh-token-jwt")
                .user(userInfo)
                .build();
    }

    @Test
    void register_Success() throws Exception {
        // Given
        when(authService.register(any(RegisterRequest.class), any(jakarta.servlet.http.HttpServletRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-jwt"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("John"))
                .andExpect(jsonPath("$.user.lastName").value("Doe"))
                .andExpect(jsonPath("$.user.role").value("BUSINESS"));

        verify(authService, times(1)).register(any(RegisterRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void register_ValidationError_MissingEmail() throws Exception {
        // Given
        registerRequest.setEmail(null);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void register_ValidationError_InvalidEmail() throws Exception {
        // Given
        registerRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void register_ValidationError_ShortPassword() throws Exception {
        // Given
        registerRequest.setPassword("123");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void register_ServiceException() throws Exception {
        // Given
        when(authService.register(any(RegisterRequest.class), any(jakarta.servlet.http.HttpServletRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already in use"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).register(any(RegisterRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void login_Success() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class), any(jakarta.servlet.http.HttpServletRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-jwt"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));

        verify(authService, times(1)).login(any(LoginRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void login_ValidationError_MissingEmail() throws Exception {
        // Given
        loginRequest.setEmail(null);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void login_ValidationError_MissingPassword() throws Exception {
        // Given
        loginRequest.setPassword(null);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class), any(jakarta.servlet.http.HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

        verify(authService, times(1)).login(any(LoginRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void refresh_Success() throws Exception {
        // Given
        String refreshToken = "valid-refresh-token";
        when(authService.refreshToken(refreshToken)).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-jwt"));

        verify(authService, times(1)).refreshToken(refreshToken);
    }

    @Test
    void refresh_MissingRefreshToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).refreshToken(any());
    }

    @Test
    void refresh_EmptyRefreshToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).refreshToken(any());
    }

    @Test
    void refresh_InvalidRefreshToken() throws Exception {
        // Given
        String invalidToken = "invalid-token";
        when(authService.refreshToken(invalidToken))
                .thenThrow(new SecurityException("Invalid refresh token"));

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + invalidToken + "\"}"))
                .andExpect(status().isForbidden());

        verify(authService, times(1)).refreshToken(invalidToken);
    }

    @Test
    void logout_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        // Note: logout ne fait aucun appel au service (gestion côté client)
        verifyNoInteractions(authService);
    }

    @Test
    void register_AllFieldsPresent() throws Exception {
        // Given
        RegisterRequest completeRequest = RegisterRequest.builder()
                .email("complete@example.com")
                .password("SecurePass123")
                .firstName("Jane")
                .lastName("Smith")
                .phone("+33687654321")
                .businessName("Complete Salon")
                .build();

        when(authService.register(any(RegisterRequest.class), any(jakarta.servlet.http.HttpServletRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user").exists());

        verify(authService, times(1)).register(any(RegisterRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void login_CaseInsensitiveEmail() throws Exception {
        // Given
        LoginRequest uppercaseEmail = LoginRequest.builder()
                .email("TEST@EXAMPLE.COM")
                .password("Password123!")
                .build();

        when(authService.login(any(LoginRequest.class), any(jakarta.servlet.http.HttpServletRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uppercaseEmail)))
                .andExpect(status().isOk());

        verify(authService, times(1)).login(any(LoginRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void refresh_NullRefreshToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":null}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).refreshToken(any());
    }

    @Test
    void register_ContentTypeNotJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not json"))
                .andExpect(status().isUnsupportedMediaType());

        verify(authService, never()).register(any(RegisterRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    void login_EmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }
}
