package com.booking.api.controller;

import com.booking.api.dto.request.ServiceRequest;
import com.booking.api.dto.response.ServiceResponse;
import com.booking.api.service.JwtService;
import com.booking.api.service.ServiceService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour ServiceController
 * Test des endpoints CRUD de gestion des services
 */
@WebMvcTest(ServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServiceService serviceService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private com.booking.api.service.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private com.booking.api.service.AuditService auditService;

    private ServiceRequest serviceRequest;
    private ServiceResponse serviceResponse;
    private UUID serviceId;

    @BeforeEach
    void setUp() {
        serviceId = UUID.randomUUID();

        serviceRequest = ServiceRequest.builder()
                .name("Haircut")
                .description("Professional haircut service")
                .durationMinutes(60)
                .price(new BigDecimal("30.00"))
                .color("#3b82f6")
                .isActive(true)
                .displayOrder(1)
                .build();

        serviceResponse = ServiceResponse.builder()
                .id(serviceId)
                .name("Haircut")
                .description("Professional haircut service")
                .durationMinutes(60)
                .price(new BigDecimal("30.00"))
                .color("#3b82f6")
                .isActive(true)
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== GET /api/services ====================

    @Test
    void getMyServices_Success() throws Exception {
        // Given
        ServiceResponse service2 = ServiceResponse.builder()
                .id(UUID.randomUUID())
                .name("Hair Coloring")
                .description("Professional hair coloring")
                .durationMinutes(90)
                .price(new BigDecimal("50.00"))
                .color("#ef4444")
                .isActive(true)
                .displayOrder(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<ServiceResponse> services = Arrays.asList(serviceResponse, service2);
        when(serviceService.getMyServices()).thenReturn(services);

        // When & Then
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").value("Haircut"))
                .andExpect(jsonPath("$[0].price").value(30.00))
                .andExpect(jsonPath("$[0].durationMinutes").value(60))
                .andExpect(jsonPath("$[1].name").value("Hair Coloring"))
                .andExpect(jsonPath("$[1].price").value(50.00));

        verify(serviceService, times(1)).getMyServices();
    }

    @Test
    void getMyServices_EmptyList() throws Exception {
        // Given
        when(serviceService.getMyServices()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(serviceService, times(1)).getMyServices();
    }

    @Test
    void getMyServices_ServiceException() throws Exception {
        // Given
        when(serviceService.getMyServices())
                .thenThrow(new RuntimeException("Failed to retrieve services"));

        // When & Then
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isInternalServerError());

        verify(serviceService, times(1)).getMyServices();
    }

    // ==================== GET /api/services/{id} ====================

    @Test
    void getServiceById_Success() throws Exception {
        // Given
        when(serviceService.getServiceById(serviceId)).thenReturn(serviceResponse);

        // When & Then
        mockMvc.perform(get("/api/services/{id}", serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceId.toString()))
                .andExpect(jsonPath("$.name").value("Haircut"))
                .andExpect(jsonPath("$.description").value("Professional haircut service"))
                .andExpect(jsonPath("$.durationMinutes").value(60))
                .andExpect(jsonPath("$.price").value(30.00))
                .andExpect(jsonPath("$.color").value("#3b82f6"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.displayOrder").value(1));

        verify(serviceService, times(1)).getServiceById(serviceId);
    }

    @Test
    void getServiceById_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(serviceService.getServiceById(nonExistentId))
                .thenThrow(new RuntimeException("Service not found"));

        // When & Then
        mockMvc.perform(get("/api/services/{id}", nonExistentId))
                .andExpect(status().isInternalServerError());

        verify(serviceService, times(1)).getServiceById(nonExistentId);
    }

    @Test
    void getServiceById_InvalidUUID() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/services/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).getServiceById(any());
    }

    @Test
    void getServiceById_Unauthorized() throws Exception {
        // Given
        when(serviceService.getServiceById(serviceId))
                .thenThrow(new RuntimeException("Unauthorized access"));

        // When & Then
        mockMvc.perform(get("/api/services/{id}", serviceId))
                .andExpect(status().isInternalServerError());

        verify(serviceService, times(1)).getServiceById(serviceId);
    }

    // ==================== POST /api/services ====================

    @Test
    void createService_Success() throws Exception {
        // Given
        when(serviceService.createService(any(ServiceRequest.class))).thenReturn(serviceResponse);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Haircut"))
                .andExpect(jsonPath("$.description").value("Professional haircut service"))
                .andExpect(jsonPath("$.durationMinutes").value(60))
                .andExpect(jsonPath("$.price").value(30.00))
                .andExpect(jsonPath("$.color").value("#3b82f6"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(serviceService, times(1)).createService(any(ServiceRequest.class));
    }

    @Test
    void createService_ValidationError_MissingName() throws Exception {
        // Given
        serviceRequest.setName(null);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_ValidationError_BlankName() throws Exception {
        // Given
        serviceRequest.setName("");

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_ValidationError_NameTooLong() throws Exception {
        // Given
        serviceRequest.setName("a".repeat(256));

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_ValidationError_DescriptionTooLong() throws Exception {
        // Given
        serviceRequest.setDescription("a".repeat(1001));

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_ValidationError_MissingDuration() throws Exception {
        // Given
        serviceRequest.setDurationMinutes(null);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_ValidationError_DurationTooShort() throws Exception {
        // Given
        serviceRequest.setDurationMinutes(4);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_ValidationError_DurationTooLong() throws Exception {
        // Given
        serviceRequest.setDurationMinutes(481);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_ValidationError_MissingPrice() throws Exception {
        // Given
        serviceRequest.setPrice(null);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_ValidationError_NegativePrice() throws Exception {
        // Given
        serviceRequest.setPrice(new BigDecimal("-10.00"));

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_ValidationError_InvalidColorFormat() throws Exception {
        // Given
        serviceRequest.setColor("blue");

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_ValidationError_InvalidHexColor() throws Exception {
        // Given
        serviceRequest.setColor("#GGGGGG");

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_MinimalFields() throws Exception {
        // Given
        ServiceRequest minimalRequest = ServiceRequest.builder()
                .name("Basic Service")
                .durationMinutes(30)
                .price(new BigDecimal("20.00"))
                .build();

        ServiceResponse minimalResponse = ServiceResponse.builder()
                .id(UUID.randomUUID())
                .name("Basic Service")
                .durationMinutes(30)
                .price(new BigDecimal("20.00"))
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(serviceService.createService(any(ServiceRequest.class))).thenReturn(minimalResponse);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Basic Service"))
                .andExpect(jsonPath("$.durationMinutes").value(30))
                .andExpect(jsonPath("$.price").value(20.00));

        verify(serviceService, times(1)).createService(any(ServiceRequest.class));
    }

    @Test
    void createService_EmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_InvalidContentType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not json"))
                .andExpect(status().isUnsupportedMediaType());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void createService_ServiceException() throws Exception {
        // Given
        when(serviceService.createService(any(ServiceRequest.class)))
                .thenThrow(new RuntimeException("Failed to create service"));

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isInternalServerError());

        verify(serviceService, times(1)).createService(any(ServiceRequest.class));
    }

    // ==================== PUT /api/services/{id} ====================

    @Test
    void updateService_Success() throws Exception {
        // Given
        ServiceRequest updateRequest = ServiceRequest.builder()
                .name("Updated Haircut")
                .description("Updated professional haircut service")
                .durationMinutes(45)
                .price(new BigDecimal("35.00"))
                .color("#10b981")
                .isActive(true)
                .displayOrder(2)
                .build();

        ServiceResponse updatedResponse = ServiceResponse.builder()
                .id(serviceId)
                .name("Updated Haircut")
                .description("Updated professional haircut service")
                .durationMinutes(45)
                .price(new BigDecimal("35.00"))
                .color("#10b981")
                .isActive(true)
                .displayOrder(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(serviceService.updateService(eq(serviceId), any(ServiceRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Haircut"))
                .andExpect(jsonPath("$.description").value("Updated professional haircut service"))
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.price").value(35.00))
                .andExpect(jsonPath("$.color").value("#10b981"))
                .andExpect(jsonPath("$.displayOrder").value(2));

        verify(serviceService, times(1)).updateService(eq(serviceId), any(ServiceRequest.class));
    }

    @Test
    void updateService_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(serviceService.updateService(eq(nonExistentId), any(ServiceRequest.class)))
                .thenThrow(new RuntimeException("Service not found"));

        // When & Then
        mockMvc.perform(put("/api/services/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isInternalServerError());

        verify(serviceService, times(1)).updateService(eq(nonExistentId), any(ServiceRequest.class));
    }

    @Test
    void updateService_ValidationError_MissingName() throws Exception {
        // Given
        serviceRequest.setName(null);

        // When & Then
        mockMvc.perform(put("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).updateService(any(), any());
    }

    @Test
    void updateService_ValidationError_InvalidPrice() throws Exception {
        // Given
        serviceRequest.setPrice(new BigDecimal("-5.00"));

        // When & Then
        mockMvc.perform(put("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).updateService(any(), any());
    }

    @Test
    void updateService_InvalidUUID() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/services/{id}", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).updateService(any(), any());
    }

    @Test
    void updateService_Unauthorized() throws Exception {
        // Given
        when(serviceService.updateService(eq(serviceId), any(ServiceRequest.class)))
                .thenThrow(new RuntimeException("Unauthorized to update this service"));

        // When & Then
        mockMvc.perform(put("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isInternalServerError());

        verify(serviceService, times(1)).updateService(eq(serviceId), any(ServiceRequest.class));
    }

    @Test
    void updateService_PartialUpdate() throws Exception {
        // Given
        ServiceRequest partialRequest = ServiceRequest.builder()
                .name("Partially Updated")
                .durationMinutes(60)
                .price(new BigDecimal("30.00"))
                .build();

        ServiceResponse partialResponse = ServiceResponse.builder()
                .id(serviceId)
                .name("Partially Updated")
                .description("Professional haircut service")
                .durationMinutes(60)
                .price(new BigDecimal("30.00"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(serviceService.updateService(eq(serviceId), any(ServiceRequest.class)))
                .thenReturn(partialResponse);

        // When & Then
        mockMvc.perform(put("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Partially Updated"))
                .andExpect(jsonPath("$.description").value("Professional haircut service"));

        verify(serviceService, times(1)).updateService(eq(serviceId), any(ServiceRequest.class));
    }

    // ==================== DELETE /api/services/{id} ====================

    @Test
    void deleteService_Success() throws Exception {
        // Given
        doNothing().when(serviceService).deleteService(serviceId);

        // When & Then
        mockMvc.perform(delete("/api/services/{id}", serviceId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(serviceService, times(1)).deleteService(serviceId);
    }

    @Test
    void deleteService_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new RuntimeException("Service not found"))
                .when(serviceService).deleteService(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/services/{id}", nonExistentId))
                .andExpect(status().isInternalServerError());

        verify(serviceService, times(1)).deleteService(nonExistentId);
    }

    @Test
    void deleteService_InvalidUUID() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/services/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).deleteService(any());
    }

    @Test
    void deleteService_Unauthorized() throws Exception {
        // Given
        doThrow(new RuntimeException("Unauthorized to delete this service"))
                .when(serviceService).deleteService(serviceId);

        // When & Then
        mockMvc.perform(delete("/api/services/{id}", serviceId))
                .andExpect(status().isInternalServerError());

        verify(serviceService, times(1)).deleteService(serviceId);
    }

    @Test
    void deleteService_AlreadyDeleted() throws Exception {
        // Given
        doThrow(new RuntimeException("Service already deleted"))
                .when(serviceService).deleteService(serviceId);

        // When & Then
        mockMvc.perform(delete("/api/services/{id}", serviceId))
                .andExpect(status().isInternalServerError());

        verify(serviceService, times(1)).deleteService(serviceId);
    }

    // ==================== Edge Cases & Additional Tests ====================

    @Test
    void createService_ValidColorFormats() throws Exception {
        // Given - lowercase hex
        serviceRequest.setColor("#abc123");
        when(serviceService.createService(any(ServiceRequest.class))).thenReturn(serviceResponse);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated());

        verify(serviceService, times(1)).createService(any(ServiceRequest.class));
    }

    @Test
    void createService_ValidColorUppercase() throws Exception {
        // Given - uppercase hex
        serviceRequest.setColor("#ABC123");
        when(serviceService.createService(any(ServiceRequest.class))).thenReturn(serviceResponse);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated());

        verify(serviceService, times(1)).createService(any(ServiceRequest.class));
    }

    @Test
    void createService_MinimumDuration() throws Exception {
        // Given
        serviceRequest.setDurationMinutes(5);
        when(serviceService.createService(any(ServiceRequest.class))).thenReturn(serviceResponse);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated());

        verify(serviceService, times(1)).createService(any(ServiceRequest.class));
    }

    @Test
    void createService_MaximumDuration() throws Exception {
        // Given
        serviceRequest.setDurationMinutes(480);
        when(serviceService.createService(any(ServiceRequest.class))).thenReturn(serviceResponse);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated());

        verify(serviceService, times(1)).createService(any(ServiceRequest.class));
    }

    @Test
    void createService_ZeroPrice() throws Exception {
        // Given
        serviceRequest.setPrice(new BigDecimal("0.00"));
        when(serviceService.createService(any(ServiceRequest.class))).thenReturn(serviceResponse);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated());

        verify(serviceService, times(1)).createService(any(ServiceRequest.class));
    }

    @Test
    void createService_PriceWithDecimals() throws Exception {
        // Given
        serviceRequest.setPrice(new BigDecimal("29.99"));
        when(serviceService.createService(any(ServiceRequest.class))).thenReturn(serviceResponse);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated());

        verify(serviceService, times(1)).createService(any(ServiceRequest.class));
    }

    @Test
    void getMyServices_OnlyActiveServices() throws Exception {
        // Given
        ServiceResponse activeService = ServiceResponse.builder()
                .id(UUID.randomUUID())
                .name("Active Service")
                .durationMinutes(60)
                .price(new BigDecimal("30.00"))
                .isActive(true)
                .build();

        List<ServiceResponse> services = Collections.singletonList(activeService);
        when(serviceService.getMyServices()).thenReturn(services);

        // When & Then
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isActive").value(true));

        verify(serviceService, times(1)).getMyServices();
    }

    @Test
    void updateService_ChangeActiveStatus() throws Exception {
        // Given
        serviceRequest.setIsActive(false);
        ServiceResponse inactiveResponse = ServiceResponse.builder()
                .id(serviceId)
                .name("Haircut")
                .durationMinutes(60)
                .price(new BigDecimal("30.00"))
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(serviceService.updateService(eq(serviceId), any(ServiceRequest.class)))
                .thenReturn(inactiveResponse);

        // When & Then
        mockMvc.perform(put("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(serviceService, times(1)).updateService(eq(serviceId), any(ServiceRequest.class));
    }

    @Test
    void createService_MaxNameLength() throws Exception {
        // Given
        serviceRequest.setName("a".repeat(255));
        when(serviceService.createService(any(ServiceRequest.class))).thenReturn(serviceResponse);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated());

        verify(serviceService, times(1)).createService(any(ServiceRequest.class));
    }

    @Test
    void createService_MaxDescriptionLength() throws Exception {
        // Given
        serviceRequest.setDescription("a".repeat(1000));
        when(serviceService.createService(any(ServiceRequest.class))).thenReturn(serviceResponse);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated());

        verify(serviceService, times(1)).createService(any(ServiceRequest.class));
    }
}
