package com.booking.api.service;

import com.booking.api.dto.request.ScheduleRequest;
import com.booking.api.dto.response.ScheduleResponse;
import com.booking.api.model.Business;
import com.booking.api.model.Schedule;
import com.booking.api.model.User;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.ScheduleRepository;
import com.booking.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScheduleService
 * Tests schedule management with time validation and soft delete
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ScheduleService scheduleService;

    private static final String TEST_EMAIL = "owner@test.com";
    private static final UUID SCHEDULE_ID = UUID.randomUUID();

    private User testUser;
    private Business testBusiness;
    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email(TEST_EMAIL)
                .firstName("John")
                .lastName("Doe")
                .build();

        testBusiness = Business.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .businessName("Test Salon")
                .slug("test-salon")
                .isActive(true)
                .build();

        testSchedule = Schedule.builder()
                .id(SCHEDULE_ID)
                .business(testBusiness)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .isActive(true)
                .build();

        // Mock security context
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(TEST_EMAIL);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getMySchedules_Success() {
        // Given
        Schedule tuesdaySchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .isActive(true)
                .build();

        List<Schedule> schedules = Arrays.asList(testSchedule, tuesdaySchedule);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findByBusinessId(testBusiness.getId())).thenReturn(schedules);

        // When
        List<ScheduleResponse> responses = scheduleService.getMySchedules();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("MONDAY", responses.get(0).getDayOfWeek());
        assertEquals("TUESDAY", responses.get(1).getDayOfWeek());
        verify(scheduleRepository).findByBusinessId(testBusiness.getId());
    }

    @Test
    void getMySchedules_EmptyList() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findByBusinessId(testBusiness.getId())).thenReturn(Collections.emptyList());

        // When
        List<ScheduleResponse> responses = scheduleService.getMySchedules();

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getMySchedules_UserNotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> scheduleService.getMySchedules());

        assertEquals("User not found", exception.getMessage());
        verify(scheduleRepository, never()).findByBusinessId(any());
    }

    @Test
    void getPublicSchedules_Success() {
        // Given
        String slug = "test-salon";
        List<Schedule> schedules = Arrays.asList(testSchedule);

        when(businessRepository.findBySlug(slug)).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findByBusinessIdAndIsActiveTrue(testBusiness.getId())).thenReturn(schedules);

        // When
        List<ScheduleResponse> responses = scheduleService.getPublicSchedules(slug);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("MONDAY", responses.get(0).getDayOfWeek());
        verify(scheduleRepository).findByBusinessIdAndIsActiveTrue(testBusiness.getId());
    }

    @Test
    void getPublicSchedules_BusinessNotFound() {
        // Given
        String invalidSlug = "nonexistent";

        when(businessRepository.findBySlug(invalidSlug)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> scheduleService.getPublicSchedules(invalidSlug));

        assertEquals("Business not found", exception.getMessage());
        verify(scheduleRepository, never()).findByBusinessIdAndIsActiveTrue(any());
    }

    @Test
    void getPublicSchedules_OnlyActiveSchedules() {
        // Given
        String slug = "test-salon";

        List<Schedule> activeSchedules = Arrays.asList(testSchedule); // Only active

        when(businessRepository.findBySlug(slug)).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findByBusinessIdAndIsActiveTrue(testBusiness.getId())).thenReturn(activeSchedules);

        // When
        List<ScheduleResponse> responses = scheduleService.getPublicSchedules(slug);

        // Then
        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getIsActive());
    }

    @Test
    void createOrUpdateSchedule_CreateNew() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .dayOfWeek("WEDNESDAY")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .slotDurationMinutes(45)
                .isActive(true)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.WEDNESDAY))
                .thenReturn(Optional.empty());
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> {
            Schedule saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // When
        ScheduleResponse response = scheduleService.createOrUpdateSchedule(request);

        // Then
        assertNotNull(response);
        assertEquals("WEDNESDAY", response.getDayOfWeek());
        assertEquals(LocalTime.of(8, 0), response.getStartTime());
        assertEquals(LocalTime.of(18, 0), response.getEndTime());
        assertEquals(45, response.getSlotDurationMinutes());
        assertTrue(response.getIsActive());

        ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleRepository).save(scheduleCaptor.capture());
        Schedule saved = scheduleCaptor.getValue();
        assertEquals(testBusiness, saved.getBusiness());
        assertEquals(DayOfWeek.WEDNESDAY, saved.getDayOfWeek());
    }

    @Test
    void createOrUpdateSchedule_UpdateExisting() {
        // Given - mock repository responses
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Build the ScheduleRequest
        ScheduleRequest request = ScheduleRequest.builder()
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(8, 30))
                .endTime(LocalTime.of(18, 30))
                .slotDurationMinutes(60)
                .isActive(true)
                .build();

        // When - call the service method
        ScheduleResponse updatedSchedule = scheduleService.createOrUpdateSchedule(request);

        // Then - verify the returned schedule
        assertEquals(LocalTime.of(8, 30), updatedSchedule.getStartTime());
        assertEquals(LocalTime.of(18, 30), updatedSchedule.getEndTime());
        assertEquals(60, updatedSchedule.getSlotDurationMinutes());

        // Verify repository save was called
        verify(scheduleRepository).save(testSchedule);

        // Optional - check the original object was updated
        assertEquals(LocalTime.of(8, 30), testSchedule.getStartTime());
        assertEquals(LocalTime.of(18, 30), testSchedule.getEndTime());
        assertEquals(60, testSchedule.getSlotDurationMinutes());
    }

    @Test
    void createOrUpdateSchedule_WithDefaults() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .dayOfWeek("FRIDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                // No slotDurationMinutes or isActive
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.FRIDAY))
                .thenReturn(Optional.empty());
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        scheduleService.createOrUpdateSchedule(request);

        // Then
        ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleRepository).save(scheduleCaptor.capture());
        Schedule saved = scheduleCaptor.getValue();
        assertEquals(30, saved.getSlotDurationMinutes()); // Default 30
        assertTrue(saved.getIsActive()); // Default true
    }

    @Test
    void createOrUpdateSchedule_EndTimeBeforeStartTime() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(17, 0))
                .endTime(LocalTime.of(9, 0)) // Invalid: before start
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> scheduleService.createOrUpdateSchedule(request));

        assertEquals("End time must be after start time", exception.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void createOrUpdateSchedule_EndTimeEqualToStartTime() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 0)) // Invalid: same time
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> scheduleService.createOrUpdateSchedule(request));

        assertEquals("End time must be after start time", exception.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void updateSchedule_Success() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(16, 0))
                .slotDurationMinutes(45)
                .isActive(false)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(testSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ScheduleResponse response = scheduleService.updateSchedule(SCHEDULE_ID, request);

        // Then
        assertNotNull(response);
        assertEquals(LocalTime.of(10, 0), testSchedule.getStartTime());
        assertEquals(LocalTime.of(16, 0), testSchedule.getEndTime());
        assertEquals(45, testSchedule.getSlotDurationMinutes());
        assertFalse(testSchedule.getIsActive());
        verify(scheduleRepository).save(testSchedule);
    }

    @Test
    void updateSchedule_PartialUpdate() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .startTime(LocalTime.of(8, 0))
                // Only updating start time
                .build();

        LocalTime originalEndTime = testSchedule.getEndTime();
        int originalSlotDuration = testSchedule.getSlotDurationMinutes();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(testSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        scheduleService.updateSchedule(SCHEDULE_ID, request);

        // Then
        assertEquals(LocalTime.of(8, 0), testSchedule.getStartTime());
        assertEquals(originalEndTime, testSchedule.getEndTime()); // Unchanged
        assertEquals(originalSlotDuration, testSchedule.getSlotDurationMinutes()); // Unchanged
    }

    @Test
    void updateSchedule_NotFound() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .startTime(LocalTime.of(10, 0))
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> scheduleService.updateSchedule(SCHEDULE_ID, request));

        assertEquals("Schedule not found", exception.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void updateSchedule_NotOwnedByBusiness() {
        // Given
        Business otherBusiness = Business.builder()
                .id(UUID.randomUUID())
                .businessName("Other Business")
                .build();
        testSchedule.setBusiness(otherBusiness);

        ScheduleRequest request = ScheduleRequest.builder()
                .startTime(LocalTime.of(10, 0))
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(testSchedule));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> scheduleService.updateSchedule(SCHEDULE_ID, request));

        assertEquals("Schedule does not belong to your business", exception.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void updateSchedule_InvalidTimeRange() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(9, 0)) // Invalid
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(testSchedule));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> scheduleService.updateSchedule(SCHEDULE_ID, request));

        assertEquals("End time must be after start time", exception.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void deleteSchedule_Success() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(testSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertTrue(testSchedule.getIsActive()); // Initially active

        // When
        scheduleService.deleteSchedule(SCHEDULE_ID);

        // Then
        assertFalse(testSchedule.getIsActive()); // Soft-deleted
        verify(scheduleRepository).save(testSchedule);
    }

    @Test
    void deleteSchedule_NotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> scheduleService.deleteSchedule(SCHEDULE_ID));

        assertEquals("Schedule not found", exception.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void deleteSchedule_NotOwnedByBusiness() {
        // Given
        Business otherBusiness = Business.builder()
                .id(UUID.randomUUID())
                .businessName("Other Business")
                .build();
        testSchedule.setBusiness(otherBusiness);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(testSchedule));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> scheduleService.deleteSchedule(SCHEDULE_ID));

        assertEquals("Schedule does not belong to your business", exception.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void schedule_ResponseMapping() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(scheduleRepository.findByBusinessId(testBusiness.getId())).thenReturn(Arrays.asList(testSchedule));

        // When
        List<ScheduleResponse> responses = scheduleService.getMySchedules();

        // Then
        ScheduleResponse response = responses.get(0);
        assertEquals(testSchedule.getId(), response.getId());
        assertEquals(testSchedule.getDayOfWeek().name(), response.getDayOfWeek());
        assertEquals(testSchedule.getStartTime(), response.getStartTime());
        assertEquals(testSchedule.getEndTime(), response.getEndTime());
        assertEquals(testSchedule.getSlotDurationMinutes(), response.getSlotDurationMinutes());
        assertEquals(testSchedule.getIsActive(), response.getIsActive());
    }
}
