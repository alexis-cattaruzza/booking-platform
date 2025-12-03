package com.booking.api.service;

import com.booking.api.dto.gdpr.AccountDeletionRequest;
import com.booking.api.dto.gdpr.AccountDeletionResponse;
import com.booking.api.dto.gdpr.DataExportResponse;
import com.booking.api.model.*;
import com.booking.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GdprService
 * Tests GDPR compliance operations (data export, account deletion)
 */
@ExtendWith(MockitoExtension.class)
class GdprServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GdprService gdprService;

    private User testUser;
    private Business testBusiness;
    private Customer testCustomer;
    private List<Appointment> testAppointments;
    private List<Service> testServices;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .createdAt(LocalDateTime.now().minusMonths(6))
                .build();

        // Create test business
        testBusiness = Business.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .businessName("Test Business")
                .slug("test-business")
                .address("123 Test Street")
                .city("Paris")
                .postalCode("75001")
                .phone("0123456789")
                .description("Test description")
                .createdAt(LocalDateTime.now().minusMonths(6))
                .build();

        // Create test customer
        testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .firstName("Jane")
                .lastName("Smith")
                .email("customer@example.com")
                .phone("0987654321")
                .createdAt(LocalDateTime.now().minusMonths(3))
                .build();

        // Create test services
        testServices = new ArrayList<>();
        Service service = Service.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .name("Haircut")
                .description("Professional haircut")
                .price(java.math.BigDecimal.valueOf(30.0))
                .durationMinutes(30)
                .build();
        testServices.add(service);

        // Create test appointments
        testAppointments = new ArrayList<>();
        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .customer(testCustomer)
                .service(service)
                .appointmentDatetime(LocalDateTime.now().plusDays(1))
                .durationMinutes(30)
                .price(java.math.BigDecimal.valueOf(30.0))
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        testAppointments.add(appointment);
    }

    @Test
    void exportBusinessData_Success() {
        // Given
        String email = "test@example.com";
        when(businessRepository.findByEmail(email)).thenReturn(Optional.of(testBusiness));
        when(appointmentRepository.findByBusinessIdOrderByAppointmentDatetimeDesc(testBusiness.getId()))
                .thenReturn(testAppointments);
        when(serviceRepository.findByBusinessIdOrderByNameAsc(testBusiness.getId()))
                .thenReturn(testServices);

        // When
        DataExportResponse response = gdprService.exportUserData(email, "BUSINESS");

        // Then
        assertNotNull(response);
        assertEquals(testBusiness.getId().toString(), response.getUserId());
        assertEquals("BUSINESS", response.getUserType());
        assertNotNull(response.getExportDate());

        // Verify personal data
        assertEquals(email, response.getPersonalData().getEmail());
        assertEquals("John", response.getPersonalData().getFirstName());
        assertEquals("Doe", response.getPersonalData().getLastName());

        // Verify business data
        assertNotNull(response.getBusinessData());
        assertEquals("Test Business", response.getBusinessData().getBusinessName());
        assertEquals("test-business", response.getBusinessData().getSlug());
        assertEquals(1, response.getBusinessData().getServices().size());

        // Verify appointments
        assertEquals(1, response.getAppointments().size());

        verify(businessRepository, times(1)).findByEmail(email);
        verify(appointmentRepository, times(1)).findByBusinessIdOrderByAppointmentDatetimeDesc(testBusiness.getId());
        verify(serviceRepository, times(1)).findByBusinessIdOrderByNameAsc(testBusiness.getId());
    }

    @Test
    void exportCustomerData_Success() {
        // Given
        String email = "customer@example.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(testCustomer));
        when(appointmentRepository.findByCustomerIdOrderByAppointmentDatetimeDesc(testCustomer.getId()))
                .thenReturn(testAppointments);

        // When
        DataExportResponse response = gdprService.exportUserData(email, "CUSTOMER");

        // Then
        assertNotNull(response);
        assertEquals(testCustomer.getId().toString(), response.getUserId());
        assertEquals("CUSTOMER", response.getUserType());

        // Verify personal data
        assertEquals(email, response.getPersonalData().getEmail());
        assertEquals("Jane", response.getPersonalData().getFirstName());
        assertEquals("Smith", response.getPersonalData().getLastName());

        // Verify no business data for customer
        assertNull(response.getBusinessData());

        // Verify appointments
        assertEquals(1, response.getAppointments().size());

        verify(customerRepository, times(1)).findByEmail(email);
        verify(appointmentRepository, times(1)).findByCustomerIdOrderByAppointmentDatetimeDesc(testCustomer.getId());
    }

    @Test
    void exportUserData_BusinessNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(businessRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                gdprService.exportUserData(email, "BUSINESS")
        );

        verify(businessRepository, times(1)).findByEmail(email);
    }

    @Test
    void deleteBusinessAccount_Success() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        AccountDeletionRequest request = AccountDeletionRequest.builder()
                .password(password)
                .confirmDeletion(true)
                .reason("No longer needed")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(true);
        when(businessRepository.findByEmail(email)).thenReturn(Optional.of(testBusiness));
        when(appointmentRepository.findByBusinessIdAndAppointmentDatetimeAfter(eq(testBusiness.getId()), any()))
                .thenReturn(testAppointments);

        // When
        AccountDeletionResponse response = gdprService.deleteUserAccount(email, "BUSINESS", request);

        // Then
        assertNotNull(response);
        assertTrue(response.getCanRecover());
        assertNotNull(response.getDeletionDate());
        assertNotNull(response.getEffectiveDate());
        assertTrue(response.getMessage().contains("30 jours"));

        // Verify business was marked as deleted
        ArgumentCaptor<Business> businessCaptor = ArgumentCaptor.forClass(Business.class);
        verify(businessRepository, times(1)).save(businessCaptor.capture());
        Business savedBusiness = businessCaptor.getValue();
        assertNotNull(savedBusiness.getDeletedAt());
        assertTrue(savedBusiness.getEmail().contains(".deleted."));

        // Verify user email was changed
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getEmail().contains(".deleted."));

        // Verify appointments were cancelled
        verify(appointmentRepository, times(testAppointments.size())).save(any(Appointment.class));
    }

    @Test
    void deleteAccount_InvalidPassword() {
        // Given
        String email = "test@example.com";
        String wrongPassword = "wrongpassword";
        AccountDeletionRequest request = AccountDeletionRequest.builder()
                .password(wrongPassword)
                .confirmDeletion(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, testUser.getPasswordHash())).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                gdprService.deleteUserAccount(email, "BUSINESS", request)
        );

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(wrongPassword, testUser.getPasswordHash());
        verify(businessRepository, never()).save(any());
    }

    @Test
    void deleteCustomerAccount_Success() {
        // Given
        String email = "customer@example.com";
        String password = "password123";
        AccountDeletionRequest request = AccountDeletionRequest.builder()
                .password(password)
                .confirmDeletion(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(true);
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(testCustomer));
        when(appointmentRepository.findByCustomerIdAndAppointmentDatetimeAfter(eq(testCustomer.getId()), any()))
                .thenReturn(testAppointments);

        // When
        AccountDeletionResponse response = gdprService.deleteUserAccount(email, "CUSTOMER", request);

        // Then
        assertNotNull(response);
        assertTrue(response.getCanRecover());

        // Verify customer was marked as deleted
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository, times(1)).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertNotNull(savedCustomer.getDeletedAt());
        assertTrue(savedCustomer.getEmail().contains(".deleted."));
    }

    @Test
    void deleteAccount_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        AccountDeletionRequest request = AccountDeletionRequest.builder()
                .password("password")
                .confirmDeletion(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                gdprService.deleteUserAccount(email, "BUSINESS", request)
        );

        verify(userRepository, times(1)).findByEmail(email);
    }
}
