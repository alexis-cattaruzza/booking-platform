package com.booking.api.service;

import com.booking.api.dto.response.AvailabilityResponse;
import com.booking.api.model.*;
import com.booking.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AvailabilityService
 * Tests slot generation, conflict detection, and business rules
 */
@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleExceptionRepository scheduleExceptionRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Business testBusiness;
    private com.booking.api.model.Service testService;
    private Schedule testSchedule;
    private static final String TEST_SLUG = "test-salon";
    private static final UUID SERVICE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testBusiness = Business.builder()
                .id(UUID.randomUUID())
                .businessName("Test Salon")
                .slug(TEST_SLUG)
                .isActive(true)
                .build();

        testService = com.booking.api.model.Service.builder()
                .id(SERVICE_ID)
                .business(testBusiness)
                .name("Haircut")
                .durationMinutes(60)
                .price(BigDecimal.valueOf(30.0))
                .isActive(true)
                .build();

        testSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .isActive(true)
                .build();
    }

    @Test
    void getAvailability_Success() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(7); // Monday in future
        LocalDate mondayDate = futureDate.with(DayOfWeek.MONDAY);

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(testBusiness.getId(), mondayDate))
                .thenReturn(false);
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(appointmentRepository.findByBusinessIdAndDateRange(eq(testBusiness.getId()), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, mondayDate);

        // Then
        assertNotNull(response);
        assertEquals(mondayDate, response.getDate());
        assertFalse(response.getAvailableSlots().isEmpty());

        // Verify all slots are available (no appointments)
        assertTrue(response.getAvailableSlots().stream()
                .allMatch(AvailabilityResponse.TimeSlot::isAvailable));
    }

    @Test
    void getAvailability_BusinessNotFound() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, date));

        assertEquals("Business not found", exception.getMessage());
        verify(serviceRepository, never()).findByIdAndBusinessId(any(), any());
    }

    @Test
    void getAvailability_ServiceNotFound() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, date));

        assertEquals("Service not found", exception.getMessage());
    }

    @Test
    void getAvailability_ServiceNotActive() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        testService.setIsActive(false);

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, date));

        assertEquals("Service is not active", exception.getMessage());
    }

    @Test
    void getAvailability_PastDate() {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(1);

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, pastDate);

        // Then
        assertNotNull(response);
        assertEquals(pastDate, response.getDate());
        assertTrue(response.getAvailableSlots().isEmpty());
    }

    @Test
    void getAvailability_ScheduleException() {
        // Given
        LocalDate exceptionDate = LocalDate.now().plusDays(1);

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(testBusiness.getId(), exceptionDate))
                .thenReturn(true);

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, exceptionDate);

        // Then
        assertNotNull(response);
        assertTrue(response.getAvailableSlots().isEmpty());
        verify(scheduleRepository, never()).findByBusinessIdAndDayOfWeek(any(), any());
    }

    @Test
    void getAvailability_NoScheduleForDay() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(testBusiness.getId(), futureDate))
                .thenReturn(false);
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), futureDate.getDayOfWeek()))
                .thenReturn(Optional.empty());

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, futureDate);

        // Then
        assertNotNull(response);
        assertTrue(response.getAvailableSlots().isEmpty());
    }

    @Test
    void getAvailability_InactiveSchedule() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(7);
        LocalDate mondayDate = futureDate.with(DayOfWeek.MONDAY);
        testSchedule.setIsActive(false);

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(testBusiness.getId(), mondayDate))
                .thenReturn(false);
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, mondayDate);

        // Then
        assertNotNull(response);
        assertTrue(response.getAvailableSlots().isEmpty());
    }

    @Test
    void getAvailability_WithExistingAppointment() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(7);
        LocalDate mondayDate = futureDate.with(DayOfWeek.MONDAY);

        Appointment existingAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .service(testService)
                .appointmentDatetime(LocalDateTime.of(mondayDate, LocalTime.of(10, 0)))
                .durationMinutes(60)
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .build();

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(testBusiness.getId(), mondayDate))
                .thenReturn(false);
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(appointmentRepository.findByBusinessIdAndDateRange(eq(testBusiness.getId()), any(), any()))
                .thenReturn(Arrays.asList(existingAppointment));

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, mondayDate);

        // Then
        assertNotNull(response);
        assertFalse(response.getAvailableSlots().isEmpty());

        // Check that 10:00 slot is unavailable
        long unavailableSlots = response.getAvailableSlots().stream()
                .filter(slot -> !slot.isAvailable())
                .count();
        assertTrue(unavailableSlots > 0, "Should have unavailable slots due to appointment");
    }

    @Test
    void getAvailability_CancelledAppointmentDoesNotBlock() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(7);
        LocalDate mondayDate = futureDate.with(DayOfWeek.MONDAY);

        Appointment cancelledAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .service(testService)
                .appointmentDatetime(LocalDateTime.of(mondayDate, LocalTime.of(10, 0)))
                .durationMinutes(60)
                .status(Appointment.AppointmentStatus.CANCELLED)
                .build();

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(testBusiness.getId(), mondayDate))
                .thenReturn(false);
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(appointmentRepository.findByBusinessIdAndDateRange(eq(testBusiness.getId()), any(), any()))
                .thenReturn(Arrays.asList(cancelledAppointment));

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, mondayDate);

        // Then
        assertNotNull(response);
        // All slots should be available since cancelled appointments don't block
        assertTrue(response.getAvailableSlots().stream()
                .allMatch(AvailabilityResponse.TimeSlot::isAvailable));
    }

    @Test
    void getAvailability_SlotCalculationWithServiceDuration() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(7);
        LocalDate mondayDate = futureDate.with(DayOfWeek.MONDAY);

        // Service is 60 minutes, slots every 30 minutes
        // From 9:00 to 17:00 (8 hours)
        // Expected slots: 9:00-10:00, 9:30-10:30, 10:00-11:00, etc.

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(testBusiness.getId(), mondayDate))
                .thenReturn(false);
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(appointmentRepository.findByBusinessIdAndDateRange(eq(testBusiness.getId()), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, mondayDate);

        // Then
        assertNotNull(response);
        assertFalse(response.getAvailableSlots().isEmpty());

        // First slot should start at 9:00
        AvailabilityResponse.TimeSlot firstSlot = response.getAvailableSlots().get(0);
        assertEquals(LocalTime.of(9, 0), firstSlot.getStartTime());
        assertEquals(LocalTime.of(10, 0), firstSlot.getEndTime()); // +60 minutes service duration

        // Verify slots respect service duration
        for (AvailabilityResponse.TimeSlot slot : response.getAvailableSlots()) {
            long minutes = java.time.Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
            assertEquals(60,
                minutes,
                "Each slot should be 60 minutes (service duration)");
        }
    }

    @Test
    void getAvailability_ShortService() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(7);
        LocalDate mondayDate = futureDate.with(DayOfWeek.MONDAY);

        // Short 30-minute service
        testService.setDurationMinutes(30);

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(testBusiness.getId(), mondayDate))
                .thenReturn(false);
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(appointmentRepository.findByBusinessIdAndDateRange(eq(testBusiness.getId()), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, mondayDate);

        // Then
        assertNotNull(response);

        // Should have more slots for shorter service
        // 9:00-17:00 = 8 hours = 480 minutes
        // With 30-minute service and 30-minute slots, should have many slots
        assertTrue(response.getAvailableSlots().size() > 10);
    }

    @Test
    void getAvailability_MultipleAppointmentsConflict() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(7);
        LocalDate mondayDate = futureDate.with(DayOfWeek.MONDAY);

        Appointment appointment1 = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentDatetime(LocalDateTime.of(mondayDate, LocalTime.of(9, 0)))
                .durationMinutes(60)
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .build();

        Appointment appointment2 = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentDatetime(LocalDateTime.of(mondayDate, LocalTime.of(14, 0)))
                .durationMinutes(60)
                .status(Appointment.AppointmentStatus.PENDING)
                .build();

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(testBusiness.getId(), mondayDate))
                .thenReturn(false);
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(appointmentRepository.findByBusinessIdAndDateRange(eq(testBusiness.getId()), any(), any()))
                .thenReturn(Arrays.asList(appointment1, appointment2));

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, mondayDate);

        // Then
        assertNotNull(response);

        // Should have unavailable slots at 9:00 and 14:00
        long unavailableCount = response.getAvailableSlots().stream()
                .filter(slot -> !slot.isAvailable())
                .count();
        assertTrue(unavailableCount >= 2, "Should have at least 2 unavailable slots");
    }

    @Test
    void getAvailability_EdgeCaseAppointmentOverlap() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(7);
        LocalDate mondayDate = futureDate.with(DayOfWeek.MONDAY);

        // Appointment from 10:00 to 11:00
        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentDatetime(LocalDateTime.of(mondayDate, LocalTime.of(10, 0)))
                .durationMinutes(60)
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .build();

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(testBusiness.getId(), mondayDate))
                .thenReturn(false);
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(appointmentRepository.findByBusinessIdAndDateRange(eq(testBusiness.getId()), any(), any()))
                .thenReturn(Arrays.asList(appointment));

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, mondayDate);

        // Then
        // Slot at 9:30-10:30 should be unavailable (overlaps with 10:00-11:00)
        // Slot at 10:00-11:00 should be unavailable (exact match)
        // Slot at 10:30-11:30 should be unavailable (overlaps with 10:00-11:00)
        List<AvailabilityResponse.TimeSlot> overlappingSlots = response.getAvailableSlots().stream()
                .filter(slot ->
                    (slot.getStartTime().equals(LocalTime.of(9, 30)) ||
                     slot.getStartTime().equals(LocalTime.of(10, 0)) ||
                     slot.getStartTime().equals(LocalTime.of(10, 30))))
                .toList();

        assertTrue(overlappingSlots.stream().anyMatch(slot -> !slot.isAvailable()),
                "Overlapping slots should be unavailable");
    }

    @Test
    void getAvailability_DefaultSlotDuration() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(7);
        LocalDate mondayDate = futureDate.with(DayOfWeek.MONDAY);

        testSchedule.setSlotDurationMinutes(null); // Should default to 30

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(SERVICE_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(testBusiness.getId(), mondayDate))
                .thenReturn(false);
        when(scheduleRepository.findByBusinessIdAndDayOfWeek(testBusiness.getId(), DayOfWeek.MONDAY))
                .thenReturn(Optional.of(testSchedule));
        when(appointmentRepository.findByBusinessIdAndDateRange(eq(testBusiness.getId()), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        AvailabilityResponse response = availabilityService.getAvailability(TEST_SLUG, SERVICE_ID, mondayDate);

        // Then
        assertNotNull(response);
        assertFalse(response.getAvailableSlots().isEmpty());

        // Slots should increment by 30 minutes (default)
        if (response.getAvailableSlots().size() >= 2) {
            AvailabilityResponse.TimeSlot slot1 = response.getAvailableSlots().get(0);
            AvailabilityResponse.TimeSlot slot2 = response.getAvailableSlots().get(1);
            assertEquals(30, slot2.getStartTime().toSecondOfDay() / 60 - slot1.getStartTime().toSecondOfDay() / 60);
        }
    }
}
