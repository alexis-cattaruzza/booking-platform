package com.booking.api.controller;

import com.booking.api.dto.request.AppointmentRequest;
import com.booking.api.dto.request.CustomerRequest;
import com.booking.api.dto.response.AppointmentResponse;
import com.booking.api.exception.ConflictException;
import com.booking.api.exception.NotFoundException;
import com.booking.api.model.Appointment.AppointmentStatus;
import com.booking.api.service.AppointmentService;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private com.booking.api.service.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private com.booking.api.service.AuditService auditService;

    private String businessSlug = "test-salon";
    private String token = "abc123token";

    private AppointmentRequest appointmentRequest;
    private AppointmentResponse appointmentResponse;

    @BeforeEach
    void setUp() {
        // Initialize customer request
        CustomerRequest customerRequest = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+33612345678")
                .build();

        // Initialize appointment request with customer
        appointmentRequest = AppointmentRequest.builder()
                .serviceId(UUID.randomUUID())
                .appointmentDatetime(LocalDateTime.now().plusDays(1))
                .customer(customerRequest)
                .build();

        appointmentResponse = AppointmentResponse.builder()
                .id(UUID.randomUUID())
                .appointmentDatetime(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.PENDING)
                .cancellationToken(token)
                .build();
    }

    @Test
    void createAppointment_Success() throws Exception {
        when(appointmentService.createAppointment(any(), any())).thenReturn(appointmentResponse);

        mockMvc.perform(post("/api/booking/{businessSlug}", businessSlug)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(appointmentResponse.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(appointmentService, times(1)).createAppointment(any(), any());
    }

    @Test
    void createAppointment_BusinessNotFound() throws Exception {
        when(appointmentService.createAppointment(any(), any()))
                .thenThrow(new NotFoundException("Business not found"));

        mockMvc.perform(post("/api/booking/{businessSlug}", businessSlug)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAppointment_Success() throws Exception {
        when(appointmentService.getAppointmentByToken(token)).thenReturn(appointmentResponse);

        mockMvc.perform(get("/api/booking/appointment/{cancellationToken}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentResponse.getId().toString()));
    }

    @Test
    void getAppointment_NotFound() throws Exception {
        when(appointmentService.getAppointmentByToken(token))
                .thenThrow(new NotFoundException("Appointment not found"));

        mockMvc.perform(get("/api/booking/appointment/{cancellationToken}", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelAppointment_Success() throws Exception {
        doNothing().when(appointmentService).cancelAppointment(token);

        mockMvc.perform(post("/api/booking/cancel/{cancellationToken}", token))
                .andExpect(status().isOk());
    }

    @Test
    void cancelAppointment_AlreadyCancelled() throws Exception {
        doThrow(new ConflictException("Appointment is already cancelled"))
                .when(appointmentService).cancelAppointment(token);

        mockMvc.perform(post("/api/booking/cancel/{cancellationToken}", token))
                .andExpect(status().isConflict());
    }
}
