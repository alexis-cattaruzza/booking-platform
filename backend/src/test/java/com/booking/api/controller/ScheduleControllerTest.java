package com.booking.api.controller;

import com.booking.api.dto.request.ScheduleRequest;
import com.booking.api.dto.response.ScheduleResponse;
import com.booking.api.service.JwtService;
import com.booking.api.service.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
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
 * Tests unitaires pour ScheduleController
 * Test des endpoints CRUD de gestion des horaires (business hours)
 */
@WebMvcTest(ScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private ScheduleRequest scheduleRequest;
    private ScheduleResponse scheduleResponse;
    private UUID scheduleId;

    @BeforeEach
    void setUp() {
        scheduleId = UUID.randomUUID();

        scheduleRequest = ScheduleRequest.builder()
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .isActive(true)
                .build();

        scheduleResponse = ScheduleResponse.builder()
                .id(scheduleId)
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .isActive(true)
                .build();
    }

    // ==================== GET /api/schedules ====================

    @Test
    void getMySchedules_Success() throws Exception {
        // Given
        ScheduleResponse schedule2 = ScheduleResponse.builder()
                .id(UUID.randomUUID())
                .dayOfWeek("TUESDAY")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .slotDurationMinutes(60)
                .isActive(true)
                .build();

        List<ScheduleResponse> schedules = Arrays.asList(scheduleResponse, schedule2);
        when(scheduleService.getMySchedules()).thenReturn(schedules);

        // When & Then
        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$[0].endTime").value("17:00:00"))
                .andExpect(jsonPath("$[0].slotDurationMinutes").value(30))
                .andExpect(jsonPath("$[1].dayOfWeek").value("TUESDAY"))
                .andExpect(jsonPath("$[1].startTime").value("10:00:00"))
                .andExpect(jsonPath("$[1].endTime").value("18:00:00"))
                .andExpect(jsonPath("$[1].slotDurationMinutes").value(60));

        verify(scheduleService, times(1)).getMySchedules();
    }

    @Test
    void getMySchedules_EmptyList() throws Exception {
        // Given
        when(scheduleService.getMySchedules()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(scheduleService, times(1)).getMySchedules();
    }

    @Test
    void getMySchedules_ServiceException() throws Exception {
        // Given
        when(scheduleService.getMySchedules())
                .thenThrow(new RuntimeException("Failed to retrieve schedules"));

        // When & Then
        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isInternalServerError());

        verify(scheduleService, times(1)).getMySchedules();
    }

    @Test
    void getMySchedules_AllDaysOfWeek() throws Exception {
        // Given - schedules for all days of the week
        List<ScheduleResponse> schedules = Arrays.asList(
                createScheduleResponse(UUID.randomUUID(), "MONDAY", LocalTime.of(9, 0), LocalTime.of(17, 0)),
                createScheduleResponse(UUID.randomUUID(), "TUESDAY", LocalTime.of(9, 0), LocalTime.of(17, 0)),
                createScheduleResponse(UUID.randomUUID(), "WEDNESDAY", LocalTime.of(9, 0), LocalTime.of(17, 0)),
                createScheduleResponse(UUID.randomUUID(), "THURSDAY", LocalTime.of(9, 0), LocalTime.of(17, 0)),
                createScheduleResponse(UUID.randomUUID(), "FRIDAY", LocalTime.of(9, 0), LocalTime.of(17, 0)),
                createScheduleResponse(UUID.randomUUID(), "SATURDAY", LocalTime.of(10, 0), LocalTime.of(14, 0)),
                createScheduleResponse(UUID.randomUUID(), "SUNDAY", LocalTime.of(10, 0), LocalTime.of(14, 0))
        );
        when(scheduleService.getMySchedules()).thenReturn(schedules);

        // When & Then
        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[6].dayOfWeek").value("SUNDAY"));

        verify(scheduleService, times(1)).getMySchedules();
    }

    // ==================== POST /api/schedules ====================

    @Test
    void createOrUpdateSchedule_Success() throws Exception {
        // Given
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.startTime").value("09:00:00"))
                .andExpect(jsonPath("$.endTime").value("17:00:00"))
                .andExpect(jsonPath("$.slotDurationMinutes").value(30))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_AllDaysOfWeek() throws Exception {
        // Test all valid day of week values
        String[] daysOfWeek = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};

        for (String day : daysOfWeek) {
            scheduleRequest.setDayOfWeek(day);
            scheduleResponse.setDayOfWeek(day);
            when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                    .thenReturn(scheduleResponse);

            mockMvc.perform(post("/api/schedules")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scheduleRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.dayOfWeek").value(day));
        }

        verify(scheduleService, times(7)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_ValidationError_MissingDayOfWeek() throws Exception {
        // Given
        scheduleRequest.setDayOfWeek(null);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).createOrUpdateSchedule(any());
    }

    @Test
    void createOrUpdateSchedule_ValidationError_InvalidDayOfWeek() throws Exception {
        // Given
        scheduleRequest.setDayOfWeek("INVALIDDAY");

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).createOrUpdateSchedule(any());
    }

    @Test
    void createOrUpdateSchedule_ValidationError_LowercaseDayOfWeek() throws Exception {
        // Given
        scheduleRequest.setDayOfWeek("monday");

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).createOrUpdateSchedule(any());
    }

    @Test
    void createOrUpdateSchedule_ValidationError_MissingStartTime() throws Exception {
        // Given
        scheduleRequest.setStartTime(null);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).createOrUpdateSchedule(any());
    }

    @Test
    void createOrUpdateSchedule_ValidationError_MissingEndTime() throws Exception {
        // Given
        scheduleRequest.setEndTime(null);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).createOrUpdateSchedule(any());
    }

    @Test
    void createOrUpdateSchedule_ValidationError_SlotDurationTooShort() throws Exception {
        // Given
        scheduleRequest.setSlotDurationMinutes(4);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).createOrUpdateSchedule(any());
    }

    @Test
    void createOrUpdateSchedule_ValidationError_SlotDurationTooLong() throws Exception {
        // Given
        scheduleRequest.setSlotDurationMinutes(241);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).createOrUpdateSchedule(any());
    }

    @Test
    void createOrUpdateSchedule_MinimumSlotDuration() throws Exception {
        // Given
        scheduleRequest.setSlotDurationMinutes(5);
        scheduleResponse.setSlotDurationMinutes(5);
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slotDurationMinutes").value(5));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_MaximumSlotDuration() throws Exception {
        // Given
        scheduleRequest.setSlotDurationMinutes(240);
        scheduleResponse.setSlotDurationMinutes(240);
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slotDurationMinutes").value(240));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_EmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).createOrUpdateSchedule(any());
    }

    @Test
    void createOrUpdateSchedule_InvalidContentType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not json"))
                .andExpect(status().isUnsupportedMediaType());

        verify(scheduleService, never()).createOrUpdateSchedule(any());
    }

    @Test
    void createOrUpdateSchedule_ServiceException() throws Exception {
        // Given
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenThrow(new RuntimeException("Failed to create schedule"));

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isInternalServerError());

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_MorningShift() throws Exception {
        // Given
        scheduleRequest.setStartTime(LocalTime.of(6, 0));
        scheduleRequest.setEndTime(LocalTime.of(12, 0));
        scheduleResponse.setStartTime(LocalTime.of(6, 0));
        scheduleResponse.setEndTime(LocalTime.of(12, 0));
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("06:00:00"))
                .andExpect(jsonPath("$.endTime").value("12:00:00"));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_EveningShift() throws Exception {
        // Given
        scheduleRequest.setStartTime(LocalTime.of(18, 0));
        scheduleRequest.setEndTime(LocalTime.of(23, 59));
        scheduleResponse.setStartTime(LocalTime.of(18, 0));
        scheduleResponse.setEndTime(LocalTime.of(23, 59));
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("18:00:00"))
                .andExpect(jsonPath("$.endTime").value("23:59:00"));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_24HourFormat() throws Exception {
        // Given - start at midnight
        scheduleRequest.setStartTime(LocalTime.of(0, 0));
        scheduleRequest.setEndTime(LocalTime.of(23, 59));
        scheduleResponse.setStartTime(LocalTime.of(0, 0));
        scheduleResponse.setEndTime(LocalTime.of(23, 59));
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("00:00:00"))
                .andExpect(jsonPath("$.endTime").value("23:59:00"));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_InactiveSchedule() throws Exception {
        // Given
        scheduleRequest.setIsActive(false);
        scheduleResponse.setIsActive(false);
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_MinimalFields() throws Exception {
        // Given - only required fields
        ScheduleRequest minimalRequest = ScheduleRequest.builder()
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        ScheduleResponse minimalResponse = ScheduleResponse.builder()
                .id(UUID.randomUUID())
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(minimalResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_WeekendSchedule() throws Exception {
        // Given
        scheduleRequest.setDayOfWeek("SATURDAY");
        scheduleRequest.setStartTime(LocalTime.of(10, 0));
        scheduleRequest.setEndTime(LocalTime.of(14, 0));
        scheduleResponse.setDayOfWeek("SATURDAY");
        scheduleResponse.setStartTime(LocalTime.of(10, 0));
        scheduleResponse.setEndTime(LocalTime.of(14, 0));
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek").value("SATURDAY"))
                .andExpect(jsonPath("$.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.endTime").value("14:00:00"));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    // ==================== PUT /api/schedules/{id} ====================

    @Test
    void updateSchedule_Success() throws Exception {
        // Given
        ScheduleRequest updateRequest = ScheduleRequest.builder()
                .dayOfWeek("TUESDAY")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .slotDurationMinutes(60)
                .isActive(true)
                .build();

        ScheduleResponse updatedResponse = ScheduleResponse.builder()
                .id(scheduleId)
                .dayOfWeek("TUESDAY")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .slotDurationMinutes(60)
                .isActive(true)
                .build();

        when(scheduleService.updateSchedule(eq(scheduleId), any(ScheduleRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                .andExpect(jsonPath("$.dayOfWeek").value("TUESDAY"))
                .andExpect(jsonPath("$.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.endTime").value("18:00:00"))
                .andExpect(jsonPath("$.slotDurationMinutes").value(60));

        verify(scheduleService, times(1)).updateSchedule(eq(scheduleId), any(ScheduleRequest.class));
    }

    @Test
    void updateSchedule_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(scheduleService.updateSchedule(eq(nonExistentId), any(ScheduleRequest.class)))
                .thenThrow(new RuntimeException("Schedule not found"));

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isInternalServerError());

        verify(scheduleService, times(1)).updateSchedule(eq(nonExistentId), any(ScheduleRequest.class));
    }

    @Test
    void updateSchedule_ValidationError_MissingDayOfWeek() throws Exception {
        // Given
        scheduleRequest.setDayOfWeek(null);

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).updateSchedule(any(), any());
    }

    @Test
    void updateSchedule_ValidationError_InvalidDayOfWeek() throws Exception {
        // Given
        scheduleRequest.setDayOfWeek("FUNDAY");

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).updateSchedule(any(), any());
    }

    @Test
    void updateSchedule_ValidationError_MissingStartTime() throws Exception {
        // Given
        scheduleRequest.setStartTime(null);

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).updateSchedule(any(), any());
    }

    @Test
    void updateSchedule_ValidationError_MissingEndTime() throws Exception {
        // Given
        scheduleRequest.setEndTime(null);

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).updateSchedule(any(), any());
    }

    @Test
    void updateSchedule_ValidationError_SlotDurationTooShort() throws Exception {
        // Given
        scheduleRequest.setSlotDurationMinutes(3);

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).updateSchedule(any(), any());
    }

    @Test
    void updateSchedule_ValidationError_SlotDurationTooLong() throws Exception {
        // Given
        scheduleRequest.setSlotDurationMinutes(300);

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).updateSchedule(any(), any());
    }

    @Test
    void updateSchedule_InvalidUUID() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).updateSchedule(any(), any());
    }

    @Test
    void updateSchedule_Unauthorized() throws Exception {
        // Given
        when(scheduleService.updateSchedule(eq(scheduleId), any(ScheduleRequest.class)))
                .thenThrow(new RuntimeException("Unauthorized to update this schedule"));

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isInternalServerError());

        verify(scheduleService, times(1)).updateSchedule(eq(scheduleId), any(ScheduleRequest.class));
    }

    @Test
    void updateSchedule_ChangeActiveStatus() throws Exception {
        // Given
        scheduleRequest.setIsActive(false);
        ScheduleResponse inactiveResponse = ScheduleResponse.builder()
                .id(scheduleId)
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .isActive(false)
                .build();

        when(scheduleService.updateSchedule(eq(scheduleId), any(ScheduleRequest.class)))
                .thenReturn(inactiveResponse);

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(scheduleService, times(1)).updateSchedule(eq(scheduleId), any(ScheduleRequest.class));
    }

    @Test
    void updateSchedule_ChangeSlotDuration() throws Exception {
        // Given
        scheduleRequest.setSlotDurationMinutes(15);
        scheduleResponse.setSlotDurationMinutes(15);

        when(scheduleService.updateSchedule(eq(scheduleId), any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotDurationMinutes").value(15));

        verify(scheduleService, times(1)).updateSchedule(eq(scheduleId), any(ScheduleRequest.class));
    }

    @Test
    void updateSchedule_ExtendWorkingHours() throws Exception {
        // Given - extend working hours
        scheduleRequest.setStartTime(LocalTime.of(8, 0));
        scheduleRequest.setEndTime(LocalTime.of(20, 0));
        scheduleResponse.setStartTime(LocalTime.of(8, 0));
        scheduleResponse.setEndTime(LocalTime.of(20, 0));

        when(scheduleService.updateSchedule(eq(scheduleId), any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(put("/api/schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("08:00:00"))
                .andExpect(jsonPath("$.endTime").value("20:00:00"));

        verify(scheduleService, times(1)).updateSchedule(eq(scheduleId), any(ScheduleRequest.class));
    }

    // ==================== DELETE /api/schedules/{id} ====================

    @Test
    void deleteSchedule_Success() throws Exception {
        // Given
        doNothing().when(scheduleService).deleteSchedule(scheduleId);

        // When & Then
        mockMvc.perform(delete("/api/schedules/{id}", scheduleId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(scheduleService, times(1)).deleteSchedule(scheduleId);
    }

    @Test
    void deleteSchedule_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new RuntimeException("Schedule not found"))
                .when(scheduleService).deleteSchedule(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/schedules/{id}", nonExistentId))
                .andExpect(status().isInternalServerError());

        verify(scheduleService, times(1)).deleteSchedule(nonExistentId);
    }

    @Test
    void deleteSchedule_InvalidUUID() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/schedules/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(scheduleService, never()).deleteSchedule(any());
    }

    @Test
    void deleteSchedule_Unauthorized() throws Exception {
        // Given
        doThrow(new RuntimeException("Unauthorized to delete this schedule"))
                .when(scheduleService).deleteSchedule(scheduleId);

        // When & Then
        mockMvc.perform(delete("/api/schedules/{id}", scheduleId))
                .andExpect(status().isInternalServerError());

        verify(scheduleService, times(1)).deleteSchedule(scheduleId);
    }

    @Test
    void deleteSchedule_AlreadyDeleted() throws Exception {
        // Given
        doThrow(new RuntimeException("Schedule already deleted"))
                .when(scheduleService).deleteSchedule(scheduleId);

        // When & Then
        mockMvc.perform(delete("/api/schedules/{id}", scheduleId))
                .andExpect(status().isInternalServerError());

        verify(scheduleService, times(1)).deleteSchedule(scheduleId);
    }

    // ==================== Edge Cases & Additional Tests ====================

    @Test
    void createOrUpdateSchedule_CommonSlotDurations() throws Exception {
        // Test common slot durations: 15, 30, 45, 60, 90, 120 minutes
        int[] commonDurations = {15, 30, 45, 60, 90, 120};

        for (int duration : commonDurations) {
            scheduleRequest.setSlotDurationMinutes(duration);
            scheduleResponse.setSlotDurationMinutes(duration);
            when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                    .thenReturn(scheduleResponse);

            mockMvc.perform(post("/api/schedules")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scheduleRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.slotDurationMinutes").value(duration));
        }

        verify(scheduleService, times(commonDurations.length))
                .createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_ShortWorkingDay() throws Exception {
        // Given - 2 hours working day
        scheduleRequest.setStartTime(LocalTime.of(10, 0));
        scheduleRequest.setEndTime(LocalTime.of(12, 0));
        scheduleResponse.setStartTime(LocalTime.of(10, 0));
        scheduleResponse.setEndTime(LocalTime.of(12, 0));
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.endTime").value("12:00:00"));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_LongWorkingDay() throws Exception {
        // Given - 12 hours working day
        scheduleRequest.setStartTime(LocalTime.of(7, 0));
        scheduleRequest.setEndTime(LocalTime.of(19, 0));
        scheduleResponse.setStartTime(LocalTime.of(7, 0));
        scheduleResponse.setEndTime(LocalTime.of(19, 0));
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("07:00:00"))
                .andExpect(jsonPath("$.endTime").value("19:00:00"));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_WithMinutes() throws Exception {
        // Given - start/end times with minutes (not just on the hour)
        scheduleRequest.setStartTime(LocalTime.of(9, 30));
        scheduleRequest.setEndTime(LocalTime.of(17, 45));
        scheduleResponse.setStartTime(LocalTime.of(9, 30));
        scheduleResponse.setEndTime(LocalTime.of(17, 45));
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("09:30:00"))
                .andExpect(jsonPath("$.endTime").value("17:45:00"));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void getMySchedules_OnlyActiveSchedules() throws Exception {
        // Given - mix of active and inactive schedules
        ScheduleResponse activeSchedule = ScheduleResponse.builder()
                .id(UUID.randomUUID())
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .isActive(true)
                .build();

        ScheduleResponse inactiveSchedule = ScheduleResponse.builder()
                .id(UUID.randomUUID())
                .dayOfWeek("TUESDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .isActive(false)
                .build();

        List<ScheduleResponse> schedules = Arrays.asList(activeSchedule, inactiveSchedule);
        when(scheduleService.getMySchedules()).thenReturn(schedules);

        // When & Then
        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[1].isActive").value(false));

        verify(scheduleService, times(1)).getMySchedules();
    }

    @Test
    void createOrUpdateSchedule_EarlyMorning() throws Exception {
        // Given - early morning schedule starting at 5 AM
        scheduleRequest.setStartTime(LocalTime.of(5, 0));
        scheduleRequest.setEndTime(LocalTime.of(13, 0));
        scheduleResponse.setStartTime(LocalTime.of(5, 0));
        scheduleResponse.setEndTime(LocalTime.of(13, 0));
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("05:00:00"))
                .andExpect(jsonPath("$.endTime").value("13:00:00"));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    @Test
    void createOrUpdateSchedule_LateNight() throws Exception {
        // Given - late night schedule
        scheduleRequest.setStartTime(LocalTime.of(20, 0));
        scheduleRequest.setEndTime(LocalTime.of(23, 0));
        scheduleResponse.setStartTime(LocalTime.of(20, 0));
        scheduleResponse.setEndTime(LocalTime.of(23, 0));
        when(scheduleService.createOrUpdateSchedule(any(ScheduleRequest.class)))
                .thenReturn(scheduleResponse);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("20:00:00"))
                .andExpect(jsonPath("$.endTime").value("23:00:00"));

        verify(scheduleService, times(1)).createOrUpdateSchedule(any(ScheduleRequest.class));
    }

    // ==================== Helper Methods ====================

    private ScheduleResponse createScheduleResponse(UUID id, String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return ScheduleResponse.builder()
                .id(id)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .slotDurationMinutes(30)
                .isActive(true)
                .build();
    }
}
