package com.booking.api.controller;

import com.booking.api.dto.response.AppointmentResponse;
import com.booking.api.model.Appointment;
import com.booking.api.model.Business;
import com.booking.api.model.User;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.UserRepository;
import com.booking.api.service.AppointmentService;
import com.booking.api.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AppointmentController
 * Tests endpoints for managing appointments (get, update status)
 */
@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private BusinessRepository businessRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User testUser;
    private Business testBusiness;
    private AppointmentResponse appointmentResponse1;
    private AppointmentResponse appointmentResponse2;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("business@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.UserRole.BUSINESS)
                .build();

        testBusiness = Business.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .businessName("Test Salon")
                .slug("test-salon")
                .build();

        // Setup service info
        AppointmentResponse.ServiceInfo serviceInfo = AppointmentResponse.ServiceInfo.builder()
                .id(UUID.randomUUID())
                .name("Haircut")
                .durationMinutes(60)
                .price(new BigDecimal("50.00"))
                .color("#FF5733")
                .build();

        // Setup customer info
        AppointmentResponse.CustomerInfo customerInfo = AppointmentResponse.CustomerInfo.builder()
                .id(UUID.randomUUID())
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("0612345678")
                .build();

        appointmentResponse1 = AppointmentResponse.builder()
                .id(UUID.randomUUID())
                .appointmentDatetime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .durationMinutes(60)
                .price(new BigDecimal("50.00"))
                .status(Appointment.AppointmentStatus.PENDING)
                .notes("First appointment")
                .cancellationToken("token1")
                .service(serviceInfo)
                .customer(customerInfo)
                .createdAt(LocalDateTime.now())
                .build();

        appointmentResponse2 = AppointmentResponse.builder()
                .id(UUID.randomUUID())
                .appointmentDatetime(LocalDateTime.of(2024, 1, 15, 14, 0))
                .durationMinutes(30)
                .price(new BigDecimal("30.00"))
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .notes("Second appointment")
                .cancellationToken("token2")
                .service(serviceInfo)
                .customer(customerInfo)
                .createdAt(LocalDateTime.now())
                .build();

        // Mock SecurityContext
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(testUser.getEmail());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAppointments_Success() throws Exception {
        // Given
        List<AppointmentResponse> appointments = Arrays.asList(appointmentResponse1, appointmentResponse2);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(appointmentService.getBusinessAppointments(eq(testBusiness.getId()), any(), any()))
                .thenReturn(appointments);

        // When & Then
        mockMvc.perform(get("/api/appointments")
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-01-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(appointmentResponse1.getId().toString()))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].notes").value("First appointment"))
                .andExpect(jsonPath("$[1].id").value(appointmentResponse2.getId().toString()))
                .andExpect(jsonPath("$[1].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[1].notes").value("Second appointment"));

        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(businessRepository, times(1)).findByUserId(testUser.getId());
        verify(appointmentService, times(1)).getBusinessAppointments(eq(testBusiness.getId()), any(), any());
    }

    @Test
    void getAppointments_EmptyList() throws Exception {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(appointmentService.getBusinessAppointments(eq(testBusiness.getId()), any(), any()))
                .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/appointments")
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-01-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(appointmentService, times(1)).getBusinessAppointments(eq(testBusiness.getId()), any(), any());
    }

    @Test
    void getAppointments_MissingStartParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/appointments")
                        .param("end", "2024-01-31T23:59:00"))
                .andExpect(status().isInternalServerError());

        verify(appointmentService, never()).getBusinessAppointments(any(), any(), any());
    }

    @Test
    void getAppointments_MissingEndParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/appointments")
                        .param("start", "2024-01-01T00:00:00"))
                .andExpect(status().isInternalServerError());

        verify(appointmentService, never()).getBusinessAppointments(any(), any(), any());
    }

    @Test
    void getAppointments_InvalidDateFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/appointments")
                        .param("start", "invalid-date")
                        .param("end", "2024-01-31T23:59:00"))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).getBusinessAppointments(any(), any(), any());
    }

    @Test
    void getAppointments_UserNotFound() throws Exception {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/appointments")
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-01-31T23:59:00"))
                .andExpect(status().isInternalServerError());

        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(businessRepository, never()).findByUserId(any());
        verify(appointmentService, never()).getBusinessAppointments(any(), any(), any());
    }

    @Test
    void getAppointments_BusinessNotFound() throws Exception {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/appointments")
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-01-31T23:59:00"))
                .andExpect(status().isInternalServerError());

        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(businessRepository, times(1)).findByUserId(testUser.getId());
        verify(appointmentService, never()).getBusinessAppointments(any(), any(), any());
    }

    @Test
    void updateAppointmentStatus_Success_Confirmed() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        AppointmentResponse updatedResponse = AppointmentResponse.builder()
                .id(appointmentId)
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .appointmentDatetime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .durationMinutes(60)
                .price(new BigDecimal("50.00"))
                .build();

        when(appointmentService.updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.CONFIRMED))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/appointments/{appointmentId}/status", appointmentId)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(appointmentService, times(1))
                .updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.CONFIRMED);
    }

    @Test
    void updateAppointmentStatus_Success_Completed() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        AppointmentResponse updatedResponse = AppointmentResponse.builder()
                .id(appointmentId)
                .status(Appointment.AppointmentStatus.COMPLETED)
                .appointmentDatetime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .durationMinutes(60)
                .price(new BigDecimal("50.00"))
                .build();

        when(appointmentService.updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.COMPLETED))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/appointments/{appointmentId}/status", appointmentId)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(appointmentService, times(1))
                .updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.COMPLETED);
    }

    @Test
    void updateAppointmentStatus_Success_NoShow() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        AppointmentResponse updatedResponse = AppointmentResponse.builder()
                .id(appointmentId)
                .status(Appointment.AppointmentStatus.NO_SHOW)
                .appointmentDatetime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .durationMinutes(60)
                .price(new BigDecimal("50.00"))
                .build();

        when(appointmentService.updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.NO_SHOW))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/appointments/{appointmentId}/status", appointmentId)
                        .param("status", "NO_SHOW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));

        verify(appointmentService, times(1))
                .updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.NO_SHOW);
    }

    @Test
    void updateAppointmentStatus_Success_Cancelled() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        AppointmentResponse updatedResponse = AppointmentResponse.builder()
                .id(appointmentId)
                .status(Appointment.AppointmentStatus.CANCELLED)
                .appointmentDatetime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .durationMinutes(60)
                .price(new BigDecimal("50.00"))
                .build();

        when(appointmentService.updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.CANCELLED))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/appointments/{appointmentId}/status", appointmentId)
                        .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(appointmentService, times(1))
                .updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.CANCELLED);
    }

    @Test
    void updateAppointmentStatus_MissingStatusParameter() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(put("/api/appointments/{appointmentId}/status", appointmentId))
                .andExpect(status().isInternalServerError());

        verify(appointmentService, never()).updateAppointmentStatus(any(), any());
    }

    @Test
    void updateAppointmentStatus_InvalidStatus() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(put("/api/appointments/{appointmentId}/status", appointmentId)
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).updateAppointmentStatus(any(), any());
    }

    @Test
    void updateAppointmentStatus_AppointmentNotFound() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        when(appointmentService.updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.CONFIRMED))
                .thenThrow(new RuntimeException("Appointment not found"));

        // When & Then
        mockMvc.perform(put("/api/appointments/{appointmentId}/status", appointmentId)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isInternalServerError());

        verify(appointmentService, times(1))
                .updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.CONFIRMED);
    }

    @Test
    void updateAppointmentStatus_InvalidAppointmentId() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/appointments/{appointmentId}/status", "invalid-uuid")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).updateAppointmentStatus(any(), any());
    }

    @Test
    void getAppointments_WithDifferentDateRanges() throws Exception {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(appointmentService.getBusinessAppointments(eq(testBusiness.getId()), any(), any()))
                .thenReturn(Arrays.asList(appointmentResponse1));

        // When & Then - Single day range
        mockMvc.perform(get("/api/appointments")
                        .param("start", "2024-01-15T00:00:00")
                        .param("end", "2024-01-15T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // When & Then - Multiple months range
        mockMvc.perform(get("/api/appointments")
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-03-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(appointmentService, times(2)).getBusinessAppointments(eq(testBusiness.getId()), any(), any());
    }

    @Test
    void getAppointments_ServiceException() throws Exception {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(appointmentService.getBusinessAppointments(eq(testBusiness.getId()), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/appointments")
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-01-31T23:59:00"))
                .andExpect(status().isInternalServerError());

        verify(appointmentService, times(1)).getBusinessAppointments(eq(testBusiness.getId()), any(), any());
    }

    @Test
    void updateAppointmentStatus_AllStatuses() throws Exception {
        // Test all possible status transitions
        UUID appointmentId = UUID.randomUUID();
        Appointment.AppointmentStatus[] statuses = Appointment.AppointmentStatus.values();

        for (Appointment.AppointmentStatus status : statuses) {
            AppointmentResponse response = AppointmentResponse.builder()
                    .id(appointmentId)
                    .status(status)
                    .appointmentDatetime(LocalDateTime.of(2024, 1, 15, 10, 0))
                    .durationMinutes(60)
                    .price(new BigDecimal("50.00"))
                    .build();

            when(appointmentService.updateAppointmentStatus(appointmentId, status))
                    .thenReturn(response);

            mockMvc.perform(put("/api/appointments/{appointmentId}/status", appointmentId)
                            .param("status", status.name()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(status.name()));
        }

        verify(appointmentService, times(statuses.length))
                .updateAppointmentStatus(eq(appointmentId), any(Appointment.AppointmentStatus.class));
    }
}
