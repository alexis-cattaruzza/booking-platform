package com.booking.api.controller;

import com.booking.api.dto.response.AvailabilityResponse;
import com.booking.api.service.AvailabilityService;
import com.booking.api.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AvailabilityController
 * Tests public availability checking endpoint
 */
@WebMvcTest(AvailabilityController.class)
@AutoConfigureMockMvc(addFilters = false)
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private com.booking.api.service.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private com.booking.api.service.AuditService auditService;

    private String businessSlug;
    private UUID serviceId;
    private LocalDate testDate;
    private AvailabilityResponse availabilityResponse;

    @BeforeEach
    void setUp() {
        businessSlug = "test-salon";
        serviceId = UUID.randomUUID();
        testDate = LocalDate.of(2024, 1, 15);

        // Setup time slots
        List<AvailabilityResponse.TimeSlot> timeSlots = Arrays.asList(
                AvailabilityResponse.TimeSlot.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(10, 0))
                        .available(true)
                        .build(),
                AvailabilityResponse.TimeSlot.builder()
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(11, 0))
                        .available(true)
                        .build(),
                AvailabilityResponse.TimeSlot.builder()
                        .startTime(LocalTime.of(11, 0))
                        .endTime(LocalTime.of(12, 0))
                        .available(false)
                        .build(),
                AvailabilityResponse.TimeSlot.builder()
                        .startTime(LocalTime.of(14, 0))
                        .endTime(LocalTime.of(15, 0))
                        .available(true)
                        .build()
        );

        availabilityResponse = AvailabilityResponse.builder()
                .date(testDate)
                .availableSlots(timeSlots)
                .build();
    }

    @Test
    void getAvailability_Success() throws Exception {
        // Given
        when(availabilityService.getAvailability(businessSlug, serviceId, testDate))
                .thenReturn(availabilityResponse);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2024-01-15"))
                .andExpect(jsonPath("$.availableSlots").isArray())
                .andExpect(jsonPath("$.availableSlots.length()").value(4))
                .andExpect(jsonPath("$.availableSlots[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$.availableSlots[0].endTime").value("10:00:00"))
                .andExpect(jsonPath("$.availableSlots[0].available").value(true))
                .andExpect(jsonPath("$.availableSlots[2].available").value(false));

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, testDate);
    }

    @Test
    void getAvailability_NoAvailableSlots() throws Exception {
        // Given
        AvailabilityResponse emptyResponse = AvailabilityResponse.builder()
                .date(testDate)
                .availableSlots(Arrays.asList())
                .build();

        when(availabilityService.getAvailability(businessSlug, serviceId, testDate))
                .thenReturn(emptyResponse);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2024-01-15"))
                .andExpect(jsonPath("$.availableSlots").isArray())
                .andExpect(jsonPath("$.availableSlots.length()").value(0));

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, testDate);
    }

    @Test
    void getAvailability_AllSlotsUnavailable() throws Exception {
        // Given
        List<AvailabilityResponse.TimeSlot> unavailableSlots = Arrays.asList(
                AvailabilityResponse.TimeSlot.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(10, 0))
                        .available(false)
                        .build(),
                AvailabilityResponse.TimeSlot.builder()
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(11, 0))
                        .available(false)
                        .build()
        );

        AvailabilityResponse response = AvailabilityResponse.builder()
                .date(testDate)
                .availableSlots(unavailableSlots)
                .build();

        when(availabilityService.getAvailability(businessSlug, serviceId, testDate))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSlots[0].available").value(false))
                .andExpect(jsonPath("$.availableSlots[1].available").value(false));

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, testDate);
    }

    @Test
    void getAvailability_MissingServiceId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("date", "2024-01-15"))
                .andExpect(status().isInternalServerError());

        verify(availabilityService, never()).getAvailability(any(), any(), any());
    }

    @Test
    void getAvailability_MissingDate() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString()))
                .andExpect(status().isInternalServerError());

        verify(availabilityService, never()).getAvailability(any(), any(), any());
    }

    @Test
    void getAvailability_InvalidDateFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "invalid-date"))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).getAvailability(any(), any(), any());
    }

    @Test
    void getAvailability_InvalidServiceIdFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", "invalid-uuid")
                        .param("date", "2024-01-15"))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).getAvailability(any(), any(), any());
    }

    @Test
    void getAvailability_BusinessNotFound() throws Exception {
        // Given
        when(availabilityService.getAvailability(businessSlug, serviceId, testDate))
                .thenThrow(new RuntimeException("Business not found"));

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-15"))
                .andExpect(status().isInternalServerError());

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, testDate);
    }

    @Test
    void getAvailability_ServiceNotFound() throws Exception {
        // Given
        when(availabilityService.getAvailability(businessSlug, serviceId, testDate))
                .thenThrow(new RuntimeException("Service not found"));

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-15"))
                .andExpect(status().isInternalServerError());

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, testDate);
    }

    @Test
    void getAvailability_EmptyBusinessSlug() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/availability/")
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-15"))
                .andExpect(status().isInternalServerError());

        verify(availabilityService, never()).getAvailability(any(), any(), any());
    }

    @Test
    void getAvailability_PastDate() throws Exception {
        // Given
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        AvailabilityResponse pastResponse = AvailabilityResponse.builder()
                .date(pastDate)
                .availableSlots(Arrays.asList())
                .build();

        when(availabilityService.getAvailability(businessSlug, serviceId, pastDate))
                .thenReturn(pastResponse);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2020-01-01"));

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, pastDate);
    }

    @Test
    void getAvailability_FutureDate() throws Exception {
        // Given
        LocalDate futureDate = LocalDate.of(2025, 12, 31);
        AvailabilityResponse futureResponse = AvailabilityResponse.builder()
                .date(futureDate)
                .availableSlots(availabilityResponse.getAvailableSlots())
                .build();

        when(availabilityService.getAvailability(businessSlug, serviceId, futureDate))
                .thenReturn(futureResponse);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-12-31"))
                .andExpect(jsonPath("$.availableSlots").isArray());

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, futureDate);
    }

    @Test
    void getAvailability_WeekendDate() throws Exception {
        // Given - Saturday
        LocalDate weekendDate = LocalDate.of(2024, 1, 13);
        AvailabilityResponse weekendResponse = AvailabilityResponse.builder()
                .date(weekendDate)
                .availableSlots(Arrays.asList())
                .build();

        when(availabilityService.getAvailability(businessSlug, serviceId, weekendDate))
                .thenReturn(weekendResponse);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2024-01-13"));

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, weekendDate);
    }

    @Test
    void getAvailability_DifferentBusinessSlug() throws Exception {
        // Given
        String anotherSlug = "another-salon";
        when(availabilityService.getAvailability(anotherSlug, serviceId, testDate))
                .thenReturn(availabilityResponse);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", anotherSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2024-01-15"));

        verify(availabilityService, times(1)).getAvailability(anotherSlug, serviceId, testDate);
    }

    @Test
    void getAvailability_DifferentService() throws Exception {
        // Given
        UUID anotherServiceId = UUID.randomUUID();
        when(availabilityService.getAvailability(businessSlug, anotherServiceId, testDate))
                .thenReturn(availabilityResponse);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", anotherServiceId.toString())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSlots").isArray());

        verify(availabilityService, times(1)).getAvailability(businessSlug, anotherServiceId, testDate);
    }

    @Test
    void getAvailability_ServiceException() throws Exception {
        // Given
        when(availabilityService.getAvailability(businessSlug, serviceId, testDate))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-15"))
                .andExpect(status().isInternalServerError());

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, testDate);
    }

    @Test
    void getAvailability_WithSpecialCharactersInSlug() throws Exception {
        // Given
        String slugWithDashes = "my-awesome-salon";
        when(availabilityService.getAvailability(slugWithDashes, serviceId, testDate))
                .thenReturn(availabilityResponse);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", slugWithDashes)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk());

        verify(availabilityService, times(1)).getAvailability(slugWithDashes, serviceId, testDate);
    }

    @Test
    void getAvailability_MultipleTimeSlots() throws Exception {
        // Given - Full day schedule
        List<AvailabilityResponse.TimeSlot> fullDaySlots = Arrays.asList(
                createTimeSlot(9, 0, 10, 0, true),
                createTimeSlot(10, 0, 11, 0, true),
                createTimeSlot(11, 0, 12, 0, true),
                createTimeSlot(14, 0, 15, 0, true),
                createTimeSlot(15, 0, 16, 0, false),
                createTimeSlot(16, 0, 17, 0, true),
                createTimeSlot(17, 0, 18, 0, true)
        );

        AvailabilityResponse fullDayResponse = AvailabilityResponse.builder()
                .date(testDate)
                .availableSlots(fullDaySlots)
                .build();

        when(availabilityService.getAvailability(businessSlug, serviceId, testDate))
                .thenReturn(fullDayResponse);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSlots.length()").value(7));

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, testDate);
    }

    @Test
    void getAvailability_DateAtMonthBoundary() throws Exception {
        // Given - Last day of month
        LocalDate lastDayOfMonth = LocalDate.of(2024, 1, 31);
        AvailabilityResponse response = AvailabilityResponse.builder()
                .date(lastDayOfMonth)
                .availableSlots(availabilityResponse.getAvailableSlots())
                .build();

        when(availabilityService.getAvailability(businessSlug, serviceId, lastDayOfMonth))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2024-01-31"));

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, lastDayOfMonth);
    }

    @Test
    void getAvailability_LeapYearDate() throws Exception {
        // Given - February 29 in leap year
        LocalDate leapYearDate = LocalDate.of(2024, 2, 29);
        AvailabilityResponse response = AvailabilityResponse.builder()
                .date(leapYearDate)
                .availableSlots(availabilityResponse.getAvailableSlots())
                .build();

        when(availabilityService.getAvailability(businessSlug, serviceId, leapYearDate))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/availability/{businessSlug}", businessSlug)
                        .param("serviceId", serviceId.toString())
                        .param("date", "2024-02-29"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2024-02-29"));

        verify(availabilityService, times(1)).getAvailability(businessSlug, serviceId, leapYearDate);
    }

    private AvailabilityResponse.TimeSlot createTimeSlot(int startHour, int startMinute,
                                                          int endHour, int endMinute, boolean available) {
        return AvailabilityResponse.TimeSlot.builder()
                .startTime(LocalTime.of(startHour, startMinute))
                .endTime(LocalTime.of(endHour, endMinute))
                .available(available)
                .build();
    }
}
