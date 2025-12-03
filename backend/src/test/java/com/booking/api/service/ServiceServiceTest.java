package com.booking.api.service;

import com.booking.api.dto.request.ServiceRequest;
import com.booking.api.dto.response.ServiceResponse;
import com.booking.api.model.Business;
import com.booking.api.model.Service;
import com.booking.api.model.User;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.ServiceRepository;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServiceService
 * Tests CRUD operations for business services with proper authorization checks
 */
@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ServiceService serviceService;

    private User testUser;
    private Business testBusiness;
    private Service testService;
    private final String TEST_EMAIL = "business@test.com";
    private final UUID SERVICE_ID = UUID.randomUUID();

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

        testService = Service.builder()
                .id(SERVICE_ID)
                .business(testBusiness)
                .name("Haircut")
                .description("Standard haircut")
                .durationMinutes(30)
                .price(BigDecimal.valueOf(30.00))
                .color("#3b82f6")
                .isActive(true)
                .displayOrder(0)
                .build();

        // Mock security context
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(TEST_EMAIL);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getMyServices_Success() {
        // Given
        Service service2 = Service.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .name("Coloring")
                .price(BigDecimal.valueOf(50.00))
                .durationMinutes(60)
                .isActive(true)
                .displayOrder(1)
                .build();

        List<Service> services = Arrays.asList(testService, service2);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByBusinessIdOrderByDisplayOrderAsc(testBusiness.getId())).thenReturn(services);

        // When
        List<ServiceResponse> responses = serviceService.getMyServices();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Haircut", responses.get(0).getName());
        assertEquals("Coloring", responses.get(1).getName());
        assertTrue(responses.get(0).getDisplayOrder() < responses.get(1).getDisplayOrder());

        verify(serviceRepository).findByBusinessIdOrderByDisplayOrderAsc(testBusiness.getId());
    }

    @Test
    void getMyServices_EmptyList() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByBusinessIdOrderByDisplayOrderAsc(testBusiness.getId()))
                .thenReturn(Arrays.asList());

        // When
        List<ServiceResponse> responses = serviceService.getMyServices();

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getPublicServices_Success() {
        // Given
        String slug = "test-salon";
        List<Service> services = Arrays.asList(testService);

        when(businessRepository.findBySlug(slug)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByBusinessIdAndIsActiveTrue(testBusiness.getId())).thenReturn(services);

        // When
        List<ServiceResponse> responses = serviceService.getPublicServices(slug);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Haircut", responses.get(0).getName());
        assertTrue(responses.get(0).getIsActive());

        verify(businessRepository).findBySlug(slug);
        verify(serviceRepository).findByBusinessIdAndIsActiveTrue(testBusiness.getId());
    }

    @Test
    void getPublicServices_BusinessNotFound() {
        // Given
        String slug = "nonexistent";
        when(businessRepository.findBySlug(slug)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> serviceService.getPublicServices(slug));

        assertTrue(exception.getMessage().contains("Business not found"));
        verify(serviceRepository, never()).findByBusinessIdAndIsActiveTrue(any());
    }

    @Test
    void getPublicServices_BusinessNotActive() {
        // Given
        String slug = "test-salon";
        testBusiness.setIsActive(false);
        when(businessRepository.findBySlug(slug)).thenReturn(Optional.of(testBusiness));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> serviceService.getPublicServices(slug));

        assertEquals("Business is not active", exception.getMessage());
        verify(serviceRepository, never()).findByBusinessIdAndIsActiveTrue(any());
    }

    @Test
    void getServiceById_Success() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(testService));

        // When
        ServiceResponse response = serviceService.getServiceById(SERVICE_ID);

        // Then
        assertNotNull(response);
        assertEquals(SERVICE_ID, response.getId());
        assertEquals("Haircut", response.getName());

        verify(serviceRepository).findById(SERVICE_ID);
    }

    @Test
    void getServiceById_NotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> serviceService.getServiceById(SERVICE_ID));

        assertEquals("Service not found", exception.getMessage());
    }

    @Test
    void getServiceById_NotOwnedByBusiness() {
        // Given
        Business otherBusiness = Business.builder()
                .id(UUID.randomUUID())
                .businessName("Other Business")
                .build();
        testService.setBusiness(otherBusiness);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(testService));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> serviceService.getServiceById(SERVICE_ID));

        assertEquals("Service does not belong to your business", exception.getMessage());
    }

    @Test
    void createService_Success() {
        // Given
        ServiceRequest request = ServiceRequest.builder()
                .name("New Service")
                .description("New description")
                .durationMinutes(45)
                .price(BigDecimal.valueOf(40.00))
                .color("#ff0000")
                .isActive(true)
                .displayOrder(1)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.countByBusinessIdAndIsActiveTrue(testBusiness.getId())).thenReturn(2L);
        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ServiceResponse response = serviceService.createService(request);

        // Then
        assertNotNull(response);
        assertEquals("New Service", response.getName());
        assertEquals(45, response.getDurationMinutes());
        assertEquals(BigDecimal.valueOf(40.00), response.getPrice());
        assertEquals("#ff0000", response.getColor());
        assertTrue(response.getIsActive());
        assertEquals(1, response.getDisplayOrder());

        ArgumentCaptor<Service> serviceCaptor = ArgumentCaptor.forClass(Service.class);
        verify(serviceRepository).save(serviceCaptor.capture());

        Service savedService = serviceCaptor.getValue();
        assertEquals("New Service", savedService.getName());
        assertEquals(testBusiness, savedService.getBusiness());
    }

    @Test
    void createService_WithDefaults() {
        // Given - request without optional fields
        ServiceRequest request = ServiceRequest.builder()
                .name("Basic Service")
                .durationMinutes(30)
                .price(BigDecimal.valueOf(25.00))
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.countByBusinessIdAndIsActiveTrue(testBusiness.getId())).thenReturn(3L);
        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ServiceResponse response = serviceService.createService(request);

        // Then
        assertNotNull(response);
        assertEquals("#3b82f6", response.getColor()); // Default color
        assertTrue(response.getIsActive()); // Default active
        assertEquals(3, response.getDisplayOrder()); // Calculated from count

        ArgumentCaptor<Service> serviceCaptor = ArgumentCaptor.forClass(Service.class);
        verify(serviceRepository).save(serviceCaptor.capture());

        Service savedService = serviceCaptor.getValue();
        assertEquals("#3b82f6", savedService.getColor());
        assertTrue(savedService.getIsActive());
    }

    @Test
    void updateService_Success() {
        // Given
        ServiceRequest request = ServiceRequest.builder()
                .name("Updated Service")
                .description("Updated description")
                .durationMinutes(60)
                .price(BigDecimal.valueOf(50.00))
                .color("#00ff00")
                .isActive(false)
                .displayOrder(5)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ServiceResponse response = serviceService.updateService(SERVICE_ID, request);

        // Then
        assertNotNull(response);
        verify(serviceRepository).save(testService);

        // Verify all fields were updated
        assertEquals("Updated Service", testService.getName());
        assertEquals("Updated description", testService.getDescription());
        assertEquals(60, testService.getDurationMinutes());
        assertEquals(BigDecimal.valueOf(50.00), testService.getPrice());
        assertEquals("#00ff00", testService.getColor());
        assertFalse(testService.getIsActive());
        assertEquals(5, testService.getDisplayOrder());
    }

    @Test
    void updateService_PartialUpdate() {
        // Given - only update name and price
        ServiceRequest request = ServiceRequest.builder()
                .name("Partial Update")
                .price(BigDecimal.valueOf(35.00))
                .build();

        String originalDescription = testService.getDescription();
        int originalDuration = testService.getDurationMinutes();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ServiceResponse response = serviceService.updateService(SERVICE_ID, request);

        // Then
        assertNotNull(response);
        assertEquals("Partial Update", testService.getName());
        assertEquals(BigDecimal.valueOf(35.00), testService.getPrice());
        // Other fields should remain unchanged
        assertEquals(originalDescription, testService.getDescription());
        assertEquals(originalDuration, testService.getDurationMinutes());
    }

    @Test
    void updateService_ServiceNotFound() {
        // Given
        ServiceRequest request = ServiceRequest.builder()
                .name("Updated")
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> serviceService.updateService(SERVICE_ID, request));

        assertEquals("Service not found", exception.getMessage());
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void updateService_NotOwnedByBusiness() {
        // Given
        Business otherBusiness = Business.builder()
                .id(UUID.randomUUID())
                .businessName("Other Business")
                .build();
        testService.setBusiness(otherBusiness);

        ServiceRequest request = ServiceRequest.builder()
                .name("Updated")
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(testService));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> serviceService.updateService(SERVICE_ID, request));

        assertEquals("Service does not belong to your business", exception.getMessage());
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deleteService_Success() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertTrue(testService.getIsActive()); // Initially active

        // When
        serviceService.deleteService(SERVICE_ID);

        // Then
        assertFalse(testService.getIsActive()); // Should be soft-deleted (deactivated)
        verify(serviceRepository).save(testService);
    }

    @Test
    void deleteService_NotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> serviceService.deleteService(SERVICE_ID));

        assertEquals("Service not found", exception.getMessage());
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deleteService_NotOwnedByBusiness() {
        // Given
        Business otherBusiness = Business.builder()
                .id(UUID.randomUUID())
                .businessName("Other Business")
                .build();
        testService.setBusiness(otherBusiness);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(testService));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> serviceService.deleteService(SERVICE_ID));

        assertEquals("Service does not belong to your business", exception.getMessage());
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void getAuthenticatedUserBusiness_UserNotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When & Then - indirectly through getMyServices
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> serviceService.getMyServices());

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getAuthenticatedUserBusiness_BusinessNotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When & Then - indirectly through getMyServices
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> serviceService.getMyServices());

        assertEquals("Business not found for user", exception.getMessage());
    }
}
