package com.booking.api.service;

import com.booking.api.dto.request.AppointmentRequest;
import com.booking.api.dto.request.CustomerRequest;
import com.booking.api.dto.response.AppointmentResponse;
import com.booking.api.model.*;
import com.booking.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppointmentService
 * Tests appointment creation, cancellation, and management
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Business testBusiness;
    private Service testService;
    private Customer testCustomer;
    private User testUser;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("business@test.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        testBusiness = Business.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .businessName("Test Salon")
                .slug("test-salon")
                .build();

        testService = Service.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .name("Haircut")
                .price(BigDecimal.valueOf(30.0))
                .durationMinutes(30)
                .isActive(true)
                .build();

        testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@test.com")
                .phone("0123456789")
                .build();

        testAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .service(testService)
                .customer(testCustomer)
                .appointmentDatetime(LocalDateTime.now().plusDays(1))
                .durationMinutes(30)
                .price(BigDecimal.valueOf(30.0))
                .status(Appointment.AppointmentStatus.PENDING)
                .cancellationToken(UUID.randomUUID().toString())
                .build();
    }

    @Test
    void createPublicAppointment_Success() {
        // Given
        String slug = "test-salon";
        CustomerRequest customerRequest = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@test.com")
                .phone("0612345678")
                .build();

        AppointmentRequest request = AppointmentRequest.builder()
                .serviceId(testService.getId())
                .appointmentDatetime(LocalDateTime.now().plusDays(1))
                .customer(customerRequest)
                .notes("First visit")
                .build();

        when(businessRepository.findBySlug(slug)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(testService.getId(), testBusiness.getId()))
                .thenReturn(Optional.of(testService));
        when(appointmentRepository.findActiveAppointmentsForLocking(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(customerService.findOrCreateCustomer(any(), any())).thenReturn(testCustomer);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        AppointmentResponse response = appointmentService.createAppointment(slug, request);

        // Then
        assertNotNull(response);
        assertEquals(testAppointment.getId(), response.getId());

        // Verify appointment was saved
        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository, times(1)).save(appointmentCaptor.capture());

        Appointment savedAppointment = appointmentCaptor.getValue();
        assertEquals(testBusiness, savedAppointment.getBusiness());
        assertEquals(testService, savedAppointment.getService());
        assertEquals(testCustomer, savedAppointment.getCustomer());
        assertEquals(Appointment.AppointmentStatus.PENDING, savedAppointment.getStatus());
        assertNotNull(savedAppointment.getCancellationToken());

        // Verify confirmation email was sent
        verify(emailService, times(1)).sendBookingConfirmation(any(Appointment.class));
    }

    @Test
    void createAppointment_BusinessNotFound() {
        // Given
        String invalidSlug = "nonexistent";
        AppointmentRequest request = AppointmentRequest.builder().build();

        when(businessRepository.findBySlug(invalidSlug)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () ->
                appointmentService.createAppointment(invalidSlug, request)
        );

        verify(businessRepository, times(1)).findBySlug(invalidSlug);
        verify(appointmentRepository, never()).save(any());
        verify(emailService, never()).sendBookingConfirmation(any());
    }

    @Test
    void createAppointment_ServiceNotFound() {
        // Given
        String slug = "test-salon";
        UUID invalidServiceId = UUID.randomUUID();
        AppointmentRequest request = AppointmentRequest.builder()
                .serviceId(invalidServiceId)
                .build();

        when(businessRepository.findBySlug(slug)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(invalidServiceId, testBusiness.getId()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () ->
                appointmentService.createAppointment(slug, request)
        );

        verify(serviceRepository, times(1)).findByIdAndBusinessId(invalidServiceId, testBusiness.getId());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void cancelAppointment_Success() {
        // Given
        String cancellationToken = "valid-token";
        when(appointmentRepository.findByCancellationToken(cancellationToken))
                .thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        appointmentService.cancelAppointment(cancellationToken);

        // Then
        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository, times(1)).save(appointmentCaptor.capture());

        Appointment cancelledAppointment = appointmentCaptor.getValue();
        assertEquals(Appointment.AppointmentStatus.CANCELLED, cancelledAppointment.getStatus());

        // Verify cancellation email was sent
        verify(emailService, times(1)).sendCancellationEmail(any(Appointment.class));
    }

    @Test
    void cancelAppointment_InvalidToken() {
        // Given
        String invalidToken = "invalid-token";
        when(appointmentRepository.findByCancellationToken(invalidToken))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () ->
                appointmentService.cancelAppointment(invalidToken)
        );

        verify(appointmentRepository, times(1)).findByCancellationToken(invalidToken);
        verify(appointmentRepository, never()).save(any());
        verify(emailService, never()).sendCancellationEmail(any());
    }

    @Test
    void cancelAppointment_AlreadyCancelled() {
        // Given
        testAppointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        String token = "test-token";
        when(appointmentRepository.findByCancellationToken(token))
                .thenReturn(Optional.of(testAppointment));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                appointmentService.cancelAppointment(token)
        );

        verify(appointmentRepository, never()).save(any());
        verify(emailService, never()).sendCancellationEmail(any());
    }

    @Test
    void appointmentHasCancellationToken() {
        // Given
        CustomerRequest customerRequest = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@test.com")
                .phone("0612345678")
                .build();

        AppointmentRequest request = AppointmentRequest.builder()
                .serviceId(testService.getId())
                .appointmentDatetime(LocalDateTime.now().plusDays(1))
                .customer(customerRequest)
                .build();

        when(businessRepository.findBySlug(anyString())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(any(), any())).thenReturn(Optional.of(testService));
        when(appointmentRepository.findActiveAppointmentsForLocking(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(customerService.findOrCreateCustomer(any(), any())).thenReturn(testCustomer);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        appointmentService.createAppointment("test-salon", request);

        // Then
        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(appointmentCaptor.capture());

        Appointment saved = appointmentCaptor.getValue();
        assertNotNull(saved.getCancellationToken());
        assertFalse(saved.getCancellationToken().isEmpty());
    }

    @Test
    void appointmentPriceMatchesService() {
        // Given
        BigDecimal servicePrice = BigDecimal.valueOf(45.50);
        testService.setPrice(servicePrice);

        CustomerRequest customerRequest = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@test.com")
                .phone("0612345678")
                .build();

        AppointmentRequest request = AppointmentRequest.builder()
                .serviceId(testService.getId())
                .appointmentDatetime(LocalDateTime.now().plusDays(1))
                .customer(customerRequest)
                .build();

        when(businessRepository.findBySlug(anyString())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByIdAndBusinessId(any(), any())).thenReturn(Optional.of(testService));
        when(appointmentRepository.findActiveAppointmentsForLocking(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(customerService.findOrCreateCustomer(any(), any())).thenReturn(testCustomer);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        appointmentService.createAppointment("test-salon", request);

        // Then
        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(appointmentCaptor.capture());

        Appointment saved = appointmentCaptor.getValue();
        assertEquals(servicePrice, saved.getPrice());
    }
}
