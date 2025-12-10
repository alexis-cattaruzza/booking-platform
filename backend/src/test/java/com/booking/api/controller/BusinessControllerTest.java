package com.booking.api.controller;

import com.booking.api.dto.request.UpdateBusinessRequest;
import com.booking.api.dto.response.BusinessResponse;
import com.booking.api.dto.response.ServiceResponse;
import com.booking.api.exception.BadRequestException;
import com.booking.api.exception.NotFoundException;
import com.booking.api.service.BusinessService;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour BusinessController
 * Test des endpoints de gestion du business
 */
@WebMvcTest(BusinessController.class)
@AutoConfigureMockMvc(addFilters = false)
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BusinessService businessService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private com.booking.api.service.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private com.booking.api.service.AuditService auditService;

    private BusinessResponse businessResponse;
    private UpdateBusinessRequest updateRequest;

    @BeforeEach
    void setUp() {
        businessResponse = BusinessResponse.builder()
                .id(UUID.randomUUID())
                .businessName("Test Salon")
                .slug("test-salon")
                .description("A test salon")
                .category("BEAUTY")
                .address("123 Test Street")
                .city("Paris")
                .postalCode("75001")
                .phone("0123456789")
                .email("salon@test.com")
                .isActive(true)
                .build();

        updateRequest = UpdateBusinessRequest.builder()
                .businessName("Updated Salon")
                .description("Updated description")
                .category("BEAUTY")
                .build();
    }

    @Test
    void getMyBusiness_Success() throws Exception {
        // Given
        when(businessService.getMyBusiness()).thenReturn(businessResponse);

        // When & Then
        mockMvc.perform(get("/api/businesses/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.businessName").value("Test Salon"))
                .andExpect(jsonPath("$.slug").value("test-salon"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(businessService, times(1)).getMyBusiness();
    }

    @Test
    void getMyBusiness_NotFound() throws Exception {
        // Given
        when(businessService.getMyBusiness())
                .thenThrow(new NotFoundException("Business not found"));

        // When & Then
        mockMvc.perform(get("/api/businesses/me"))
                .andExpect(status().isNotFound());

        verify(businessService, times(1)).getMyBusiness();
    }

    @Test
    void updateMyBusiness_Success() throws Exception {
        // Given
        BusinessResponse updatedResponse = BusinessResponse.builder()
                .id(businessResponse.getId())
                .businessName("Updated Salon")
                .slug("updated-salon")
                .description("Updated description")
                .isActive(true)
                .build();

        when(businessService.updateMyBusiness(any(UpdateBusinessRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/businesses/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Updated Salon"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(businessService, times(1)).updateMyBusiness(any(UpdateBusinessRequest.class));
    }

    @Test
    void updateMyBusiness_ValidationError() throws Exception {
        // Given
        updateRequest.setBusinessName(""); // Invalid

        when(businessService.updateMyBusiness(any(UpdateBusinessRequest.class)))
          .thenThrow(new BadRequestException("Business name cannot be empty"));

        // When & Then
        mockMvc.perform(put("/api/businesses/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBusinessBySlug_Success() throws Exception {
        // Given
        String slug = "test-salon";
        when(businessService.getBusinessBySlug(slug)).thenReturn(businessResponse);

        // When & Then
        mockMvc.perform(get("/api/businesses/{slug}", slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Test Salon"))
                .andExpect(jsonPath("$.slug").value(slug));

        verify(businessService, times(1)).getBusinessBySlug(slug);
    }

    @Test
    void getBusinessBySlug_NotFound() throws Exception {
        // Given
        String slug = "nonexistent";
        when(businessService.getBusinessBySlug(slug))
                .thenThrow(new NotFoundException("Business not found"));

        // When & Then
        mockMvc.perform(get("/api/businesses/{slug}", slug))
                .andExpect(status().isNotFound());

        verify(businessService, times(1)).getBusinessBySlug(slug);
    }

    @Test
    void getBusinessServices_Success() throws Exception {
        // Given
        String slug = "test-salon";
        ServiceResponse service1 = ServiceResponse.builder()
                .id(UUID.randomUUID())
                .name("Haircut")
                .price(new BigDecimal("30.00"))
                .durationMinutes(60)
                .isActive(true)
                .build();

        ServiceResponse service2 = ServiceResponse.builder()
                .id(UUID.randomUUID())
                .name("Coloring")
                .price(new BigDecimal("50.00"))
                .durationMinutes(90)
                .isActive(true)
                .build();

        List<ServiceResponse> services = Arrays.asList(service1, service2);
        when(businessService.getBusinessServices(slug)).thenReturn(services);

        // When & Then
        mockMvc.perform(get("/api/businesses/{slug}/services", slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Haircut"))
                .andExpect(jsonPath("$[1].name").value("Coloring"));

        verify(businessService, times(1)).getBusinessServices(slug);
    }

    @Test
    void getBusinessServices_EmptyList() throws Exception {
        // Given
        String slug = "test-salon";
        when(businessService.getBusinessServices(slug)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/businesses/{slug}/services", slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(businessService, times(1)).getBusinessServices(slug);
    }

    @Test
    void getBusinessServices_BusinessNotActive() throws Exception {
        // Given
        String slug = "inactive-salon";
        when(businessService.getBusinessServices(slug))
                .thenThrow(new BadRequestException("Business is not active"));

        // When & Then
        mockMvc.perform(get("/api/businesses/{slug}/services", slug))
                .andExpect(status().isBadRequest());

        verify(businessService, times(1)).getBusinessServices(slug);
    }

    @Test
    void updateMyBusiness_PartialUpdate() throws Exception {
        // Given
        UpdateBusinessRequest partialRequest = UpdateBusinessRequest.builder()
                .businessName("New Name Only")
                .build();

        BusinessResponse partialResponse = BusinessResponse.builder()
                .id(businessResponse.getId())
                .businessName("New Name Only")
                .slug("new-name-only")
                .description(businessResponse.getDescription())
                .build();

        when(businessService.updateMyBusiness(any(UpdateBusinessRequest.class)))
                .thenReturn(partialResponse);

        // When & Then
        mockMvc.perform(put("/api/businesses/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("New Name Only"));

        verify(businessService, times(1)).updateMyBusiness(any(UpdateBusinessRequest.class));
    }
}
