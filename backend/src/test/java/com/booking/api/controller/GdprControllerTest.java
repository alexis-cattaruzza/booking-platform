package com.booking.api.controller;

import com.booking.api.dto.gdpr.AccountDeletionRequest;
import com.booking.api.dto.gdpr.AccountDeletionResponse;
import com.booking.api.dto.gdpr.DataExportResponse;
import com.booking.api.exception.BadRequestException;
import com.booking.api.service.GdprService;
import com.booking.api.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for GdprController
 * Tests GDPR compliance endpoints with proper exception handling via GlobalExceptionHandler
 */
@WebMvcTest(GdprController.class)
@AutoConfigureMockMvc
class GdprControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GdprService gdprService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private com.booking.api.service.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private com.booking.api.service.AuditService auditService;

    private String testEmail;
    private Authentication authentication;
    private DataExportResponse dataExportResponse;
    private AccountDeletionRequest deletionRequest;
    private AccountDeletionResponse deletionResponse;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";

        // Create mock authentication
        authentication = new UsernamePasswordAuthenticationToken(
                testEmail,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Setup data export response
        DataExportResponse.PersonalData personalData = DataExportResponse.PersonalData.builder()
                .email(testEmail)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+33612345678")
                .createdAt(LocalDateTime.now())
                .build();

        dataExportResponse = DataExportResponse.builder()
                .exportDate(LocalDateTime.now().toString())
                .userId("123")
                .userType("BUSINESS")
                .personalData(personalData)
                .appointments(List.of())
                .businessData(null)
                .notifications(List.of())
                .activityLogs(List.of())
                .build();

        // Setup deletion request
        deletionRequest = AccountDeletionRequest.builder()
                .password("Password123")
                .confirmDeletion(true)
                .reason("No longer need the service")
                .build();

        // Setup deletion response
        deletionResponse = AccountDeletionResponse.builder()
                .message("Account will be deleted in 30 days")
                .deletionDate(LocalDateTime.now())
                .effectiveDate(LocalDateTime.now().plusDays(30))
                .canRecover(true)
                .build();
    }

    @Test
    void exportUserData_Success() throws Exception {
        // Given
        when(gdprService.exportUserData(eq(testEmail), eq("BUSINESS")))
                .thenReturn(dataExportResponse);

        // When & Then
        mockMvc.perform(get("/api/gdpr/export")
                        .param("userType", "BUSINESS")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(gdprService, times(1)).exportUserData(testEmail, "BUSINESS");
    }

    @Test
    void exportUserData_CustomerType() throws Exception {
        // Given
        DataExportResponse customerData = DataExportResponse.builder()
                .exportDate(LocalDateTime.now().toString())
                .userId("456")
                .userType("CUSTOMER")
                .personalData(dataExportResponse.getPersonalData())
                .appointments(List.of())
                .businessData(null)
                .notifications(List.of())
                .activityLogs(List.of())
                .build();

        when(gdprService.exportUserData(eq(testEmail), eq("CUSTOMER")))
                .thenReturn(customerData);

        // When & Then
        mockMvc.perform(get("/api/gdpr/export")
                        .param("userType", "CUSTOMER")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));

        verify(gdprService, times(1)).exportUserData(testEmail, "CUSTOMER");
    }

    @Test
    void exportUserData_MissingUserType() throws Exception {
        // Given - GdprService throws BadRequestException → GlobalExceptionHandler → 400
        when(gdprService.exportUserData(eq(testEmail), isNull()))
                .thenThrow(new BadRequestException("userType is required"));

        // When & Then
        mockMvc.perform(get("/api/gdpr/export")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(gdprService, times(1)).exportUserData(eq(testEmail), isNull());
    }

    @Test
    void exportUserData_InvalidUserType() throws Exception {
        // Given - BadRequestException → 400
        when(gdprService.exportUserData(eq(testEmail), eq("INVALID")))
                .thenThrow(new BadRequestException("Invalid userType"));

        // When & Then
        mockMvc.perform(get("/api/gdpr/export")
                        .param("userType", "INVALID")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(gdprService, times(1)).exportUserData(testEmail, "INVALID");
    }

    @Test
    void exportUserData_UserNotFound() throws Exception {
        // Given - IllegalArgumentException → GlobalExceptionHandler → 400
        when(gdprService.exportUserData(eq(testEmail), eq("BUSINESS")))
                .thenThrow(new IllegalArgumentException("Business not found"));

        // When & Then
        mockMvc.perform(get("/api/gdpr/export")
                        .param("userType", "BUSINESS")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(gdprService, times(1)).exportUserData(testEmail, "BUSINESS");
    }

    @Test
    void deleteAccount_Success() throws Exception {
        // Given
        when(gdprService.deleteUserAccount(eq(testEmail), eq("BUSINESS"), any(AccountDeletionRequest.class)))
                .thenReturn(deletionResponse);

        // When & Then
        mockMvc.perform(post("/api/gdpr/delete")
                        .param("userType", "BUSINESS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletionRequest))
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account will be deleted in 30 days"))
                .andExpect(jsonPath("$.canRecover").value(true));

        verify(gdprService, times(1)).deleteUserAccount(eq(testEmail), eq("BUSINESS"), any(AccountDeletionRequest.class));
    }

    @Test
    void deleteAccount_WithoutConfirmation() throws Exception {
        // Given - Controller throws IllegalArgumentException → 400
        deletionRequest.setConfirmDeletion(false);

        // When & Then
        mockMvc.perform(post("/api/gdpr/delete")
                        .param("userType", "BUSINESS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletionRequest))
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(gdprService, never()).deleteUserAccount(any(), any(), any());
    }

    @Test
    void deleteAccount_MissingUserType() throws Exception {
        // Given - BadRequestException → 400
        when(gdprService.deleteUserAccount(eq(testEmail), isNull(), any(AccountDeletionRequest.class)))
                .thenThrow(new BadRequestException("userType is required"));

        // When & Then
        mockMvc.perform(post("/api/gdpr/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletionRequest))
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(gdprService, times(1)).deleteUserAccount(eq(testEmail), isNull(), any(AccountDeletionRequest.class));
    }

    @Test
    void deleteAccount_InvalidPassword() throws Exception {
        // Given - IllegalArgumentException → 400
        when(gdprService.deleteUserAccount(eq(testEmail), eq("BUSINESS"), any(AccountDeletionRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid password"));

        // When & Then
        mockMvc.perform(post("/api/gdpr/delete")
                        .param("userType", "BUSINESS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletionRequest))
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(gdprService, times(1)).deleteUserAccount(eq(testEmail), eq("BUSINESS"), any(AccountDeletionRequest.class));
    }

    @Test
    void deleteAccount_UserNotFound() throws Exception {
        // Given - IllegalArgumentException → 400
        when(gdprService.deleteUserAccount(eq(testEmail), eq("BUSINESS"), any(AccountDeletionRequest.class)))
                .thenThrow(new IllegalArgumentException("User not found"));

        // When & Then
        mockMvc.perform(post("/api/gdpr/delete")
                        .param("userType", "BUSINESS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletionRequest))
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(gdprService, times(1)).deleteUserAccount(eq(testEmail), eq("BUSINESS"), any(AccountDeletionRequest.class));
    }

    @Test
    void deleteAccount_InvalidUserType() throws Exception {
        // Given - BadRequestException → 400
        when(gdprService.deleteUserAccount(eq(testEmail), eq("INVALID"), any(AccountDeletionRequest.class)))
                .thenThrow(new BadRequestException("Invalid userType"));

        // When & Then
        mockMvc.perform(post("/api/gdpr/delete")
                        .param("userType", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletionRequest))
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(gdprService, times(1)).deleteUserAccount(eq(testEmail), eq("INVALID"), any(AccountDeletionRequest.class));
    }

    @Test
    void getGdprInfo_Success() throws Exception {
        // When & Then - Public endpoint doesn't need authentication
        mockMvc.perform(get("/api/gdpr/info")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("GDPR Compliance Information"))
                .andExpect(jsonPath("$.rights").isArray())
                .andExpect(jsonPath("$.rights.length()").value(6))
                .andExpect(jsonPath("$.deletionGracePeriodDays").value(30));

        verifyNoInteractions(gdprService);
    }

    @Test
    void deleteAccount_CustomerSuccess() throws Exception {
        // Given
        String customerEmail = "customer@example.com";
        Authentication customerAuth = new UsernamePasswordAuthenticationToken(
                customerEmail,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(gdprService.deleteUserAccount(eq(customerEmail), eq("CUSTOMER"), any(AccountDeletionRequest.class)))
                .thenReturn(deletionResponse);

        // When & Then
        mockMvc.perform(post("/api/gdpr/delete")
                        .param("userType", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletionRequest))
                        .with(authentication(customerAuth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canRecover").value(true));

        verify(gdprService, times(1)).deleteUserAccount(eq(customerEmail), eq("CUSTOMER"), any(AccountDeletionRequest.class));
    }

    @Test
    void deleteAccount_ValidationError_MissingPassword() throws Exception {
        // Given - @Valid will catch this
        deletionRequest.setPassword(null);

        // When & Then
        mockMvc.perform(post("/api/gdpr/delete")
                        .param("userType", "BUSINESS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletionRequest))
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(gdprService, never()).deleteUserAccount(any(), any(), any());
    }

    @Test
    void deleteAccount_NullConfirmation() throws Exception {
        // Given - Controller validation
        deletionRequest.setConfirmDeletion(null);

        // When & Then
        mockMvc.perform(post("/api/gdpr/delete")
                        .param("userType", "BUSINESS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletionRequest))
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(gdprService, never()).deleteUserAccount(any(), any(), any());
    }

    @Test
    void exportUserData_EmptyUserType() throws Exception {
        // Given - BadRequestException → 400
        when(gdprService.exportUserData(eq(testEmail), eq("")))
                .thenThrow(new BadRequestException("userType is required"));

        // When & Then
        mockMvc.perform(get("/api/gdpr/export")
                        .param("userType", "")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(gdprService, times(1)).exportUserData(testEmail, "");
    }
}
