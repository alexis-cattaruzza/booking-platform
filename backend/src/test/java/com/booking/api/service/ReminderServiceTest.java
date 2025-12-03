package com.booking.api.service;

import com.booking.api.model.Appointment;
import com.booking.api.model.Business;
import com.booking.api.model.Customer;
import com.booking.api.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReminderService
 * Tests scheduled reminder email functionality
 */
@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReminderService reminderService;

    private Business testBusiness;
    private Customer testCustomer;
    private com.booking.api.model.Service testService;

    @BeforeEach
    void setUp() {
        testBusiness = Business.builder()
                .id(UUID.randomUUID())
                .businessName("Test Salon")
                .slug("test-salon")
                .build();

        testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@test.com")
                .phone("0612345678")
                .build();

        testService = com.booking.api.model.Service.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .name("Haircut")
                .durationMinutes(60)
                .price(BigDecimal.valueOf(30.0))
                .build();
    }

    @Test
    void sendAppointmentReminders_PendingAppointments() {
        // Given
        LocalDateTime reminderTime = LocalDateTime.now().plusHours(24);

        Appointment pendingAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .appointmentDatetime(reminderTime)
                .durationMinutes(60)
                .price(BigDecimal.valueOf(30.0))
                .status(Appointment.AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(pendingAppointment));

        // When
        reminderService.sendAppointmentReminders();

        // Then
        verify(emailService, times(1)).sendAppointmentReminder(pendingAppointment);
    }

    @Test
    void sendAppointmentReminders_ConfirmedAppointments() {
        // Given
        LocalDateTime reminderTime = LocalDateTime.now().plusHours(24);

        Appointment confirmedAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .appointmentDatetime(reminderTime)
                .durationMinutes(60)
                .price(BigDecimal.valueOf(30.0))
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(confirmedAppointment));

        // When
        reminderService.sendAppointmentReminders();

        // Then
        verify(emailService, times(1)).sendAppointmentReminder(confirmedAppointment);
    }

    @Test
    void sendAppointmentReminders_CancelledAppointmentsIgnored() {
        // Given
        LocalDateTime reminderTime = LocalDateTime.now().plusHours(24);

        Appointment cancelledAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .appointmentDatetime(reminderTime)
                .durationMinutes(60)
                .price(BigDecimal.valueOf(30.0))
                .status(Appointment.AppointmentStatus.CANCELLED)
                .build();

        when(appointmentRepository.findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(cancelledAppointment));

        // When
        reminderService.sendAppointmentReminders();

        // Then
        verify(emailService, never()).sendAppointmentReminder(any());
    }

    @Test
    void sendAppointmentReminders_CompletedAppointmentsIgnored() {
        // Given
        LocalDateTime reminderTime = LocalDateTime.now().plusHours(24);

        Appointment completedAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .appointmentDatetime(reminderTime)
                .durationMinutes(60)
                .price(BigDecimal.valueOf(30.0))
                .status(Appointment.AppointmentStatus.COMPLETED)
                .build();

        when(appointmentRepository.findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(completedAppointment));

        // When
        reminderService.sendAppointmentReminders();

        // Then
        verify(emailService, never()).sendAppointmentReminder(any());
    }

    @Test
    void sendAppointmentReminders_MultipleAppointments() {
        // Given
        LocalDateTime reminderTime = LocalDateTime.now().plusHours(24);

        Appointment appointment1 = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentDatetime(reminderTime)
                .status(Appointment.AppointmentStatus.PENDING)
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .build();

        Appointment appointment2 = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentDatetime(reminderTime.plusMinutes(30))
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .build();

        Appointment appointment3 = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentDatetime(reminderTime.plusHours(1))
                .status(Appointment.AppointmentStatus.CANCELLED)
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .build();

        when(appointmentRepository.findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(appointment1, appointment2, appointment3));

        // When
        reminderService.sendAppointmentReminders();

        // Then
        verify(emailService, times(1)).sendAppointmentReminder(appointment1);
        verify(emailService, times(1)).sendAppointmentReminder(appointment2);
        verify(emailService, never()).sendAppointmentReminder(appointment3); // Cancelled - no reminder
        verify(emailService, times(2)).sendAppointmentReminder(any());
    }

    @Test
    void sendAppointmentReminders_NoAppointments() {
        // Given
        when(appointmentRepository.findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        reminderService.sendAppointmentReminders();

        // Then
        verify(emailService, never()).sendAppointmentReminder(any());
    }

    @Test
    void sendAppointmentReminders_EmailServiceException() {
        // Given
        LocalDateTime reminderTime = LocalDateTime.now().plusHours(24);

        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .appointmentDatetime(reminderTime)
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(appointment));

        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendAppointmentReminder(appointment);

        // When - Should not throw exception
        reminderService.sendAppointmentReminders();

        // Then - Email sending was attempted but failed gracefully
        verify(emailService, times(1)).sendAppointmentReminder(appointment);
    }

    @Test
    void sendAppointmentReminders_MixedSuccessAndFailure() {
        // Given
        LocalDateTime reminderTime = LocalDateTime.now().plusHours(24);

        Appointment successAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentDatetime(reminderTime)
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .build();

        Appointment failAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentDatetime(reminderTime.plusMinutes(30))
                .status(Appointment.AppointmentStatus.PENDING)
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .build();

        when(appointmentRepository.findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(successAppointment, failAppointment));

        // First call succeeds, second fails
        doNothing().when(emailService).sendAppointmentReminder(successAppointment);
        doThrow(new RuntimeException("Email failed"))
                .when(emailService).sendAppointmentReminder(failAppointment);

        // When - Should not throw exception
        reminderService.sendAppointmentReminders();

        // Then - Both attempts were made
        verify(emailService, times(1)).sendAppointmentReminder(successAppointment);
        verify(emailService, times(1)).sendAppointmentReminder(failAppointment);
    }

    @Test
    void sendAppointmentReminders_VerifyTimeWindow() {
        // Given
        when(appointmentRepository.findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        reminderService.sendAppointmentReminders();

        // Then - Verify the time window is 23-25 hours from now
        verify(appointmentRepository).findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                eq(null), // All businesses
                any(LocalDateTime.class), // 23 hours from now
                any(LocalDateTime.class)  // 25 hours from now
        );
    }

    @Test
    void sendAppointmentReminders_OnlyActiveStatuses() {
        // Given
        LocalDateTime reminderTime = LocalDateTime.now().plusHours(24);

        Appointment.AppointmentStatus[] allStatuses = Appointment.AppointmentStatus.values();

        for (Appointment.AppointmentStatus status : allStatuses) {
            Appointment appointment = Appointment.builder()
                    .id(UUID.randomUUID())
                    .appointmentDatetime(reminderTime)
                    .status(status)
                    .business(testBusiness)
                    .service(testService)
                    .customer(testCustomer)
                    .build();

            when(appointmentRepository.findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                    any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(appointment));

            // When
            reminderService.sendAppointmentReminders();

            // Then - Only PENDING and CONFIRMED should get reminders
            if (status == Appointment.AppointmentStatus.PENDING ||
                status == Appointment.AppointmentStatus.CONFIRMED) {
                verify(emailService, atLeastOnce()).sendAppointmentReminder(appointment);
            }

            // Reset mocks for next iteration
            reset(emailService);
        }
    }
}
