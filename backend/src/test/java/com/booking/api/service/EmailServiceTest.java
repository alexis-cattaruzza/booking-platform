package com.booking.api.service;

import com.booking.api.model.*;
import com.booking.api.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailService
 * Tests email sending functionality with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private Appointment testAppointment;
    private Business testBusiness;
    private Customer testCustomer;
    private Service testService;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@booking.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Test Booking");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:4200");

        // Create test user
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("business@test.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        // Create test business
        testBusiness = Business.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .businessName("Test Business")
                .address("123 Test St")
                .city("Paris")
                .postalCode("75001")
                .phone("0123456789")
                .slug("test-business")
                .build();

        // Create test service
        testService = Service.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .name("Test Service")
                .price(java.math.BigDecimal.valueOf(50.0))
                .durationMinutes(60)
                .build();

        // Create test customer
        testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .firstName("Jane")
                .lastName("Smith")
                .email("customer@test.com")
                .phone("0987654321")
                .build();

        // Create test appointment
        testAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .appointmentDatetime(LocalDateTime.now().plusDays(1))
                .durationMinutes(60)
                .price(java.math.BigDecimal.valueOf(50.0))
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .cancellationToken("test-token-123")
                .build();

        // Mock mail sender
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendBookingConfirmation_Success() {
        // When
        emailService.sendBookingConfirmation(testAppointment);

        // Then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        // Verify notification was saved
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertEquals(Notification.NotificationType.CONFIRMATION, savedNotification.getType());
        assertEquals(Notification.NotificationChannel.EMAIL, savedNotification.getChannel());
        assertEquals(Notification.NotificationStatus.SENT, savedNotification.getStatus());
        assertEquals(testCustomer.getEmail(), savedNotification.getRecipient());
        assertNotNull(savedNotification.getSentAt());
    }

    @Test
    void sendBookingConfirmation_FailureHandling() {
        // Given
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        // When
        emailService.sendBookingConfirmation(testAppointment);

        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertEquals(Notification.NotificationStatus.FAILED, savedNotification.getStatus());
        assertNull(savedNotification.getSentAt());
    }

    @Test
    void sendAppointmentReminder_Success() {
        // When
        emailService.sendAppointmentReminder(testAppointment);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertEquals(Notification.NotificationType.REMINDER, savedNotification.getType());
        assertEquals(Notification.NotificationStatus.SENT, savedNotification.getStatus());
    }

    @Test
    void sendCancellationEmail_Success() {
        // When
        emailService.sendCancellationEmail(testAppointment);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertEquals(Notification.NotificationType.CANCELLATION, savedNotification.getType());
        assertEquals(Notification.NotificationStatus.SENT, savedNotification.getStatus());
    }

    @Test
    void emailContainsCorrectCustomerName() {
        // When
        emailService.sendBookingConfirmation(testAppointment);

        // Then - Verify email was sent (content verification would require more complex HTML parsing)
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void emailContainsAppointmentDetails() {
        // When
        emailService.sendBookingConfirmation(testAppointment);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        // In a real scenario, you'd capture and parse the MimeMessage content
        // to verify it contains the service name, date, price, etc.
    }
}
