package com.booking.api.service;

import com.booking.api.dto.request.UpdateBusinessRequest;
import com.booking.api.dto.response.BusinessResponse;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BusinessService
 * Tests business profile management, public business access, and service listings
 */
@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BusinessService businessService;

    private User testUser;
    private Business testBusiness;
    private Service testService;
    private final String TEST_EMAIL = "business@test.com";
    private final String TEST_SLUG = "test-salon";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email(TEST_EMAIL)
                .firstName("John")
                .lastName("Doe")
                .role(User.UserRole.BUSINESS)
                .build();

        testBusiness = Business.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .businessName("Test Salon")
                .slug(TEST_SLUG)
                .description("A great salon")
                .address("123 Main St")
                .city("Paris")
                .postalCode("75001")
                .phone("0123456789")
                .email(TEST_EMAIL)
                .category(Business.BusinessCategory.BEAUTY)
                .isActive(true)
                .build();

        testService = Service.builder()
                .id(UUID.randomUUID())
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
    void getMyBusiness_Success() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));

        // When
        BusinessResponse response = businessService.getMyBusiness();

        // Then
        assertNotNull(response);
        assertEquals(testBusiness.getId(), response.getId());
        assertEquals(testBusiness.getBusinessName(), response.getBusinessName());
        assertEquals(testBusiness.getSlug(), response.getSlug());
        assertEquals(testBusiness.getDescription(), response.getDescription());
        assertEquals(testBusiness.getCategory().name(), response.getCategory());

        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(businessRepository).findByUserId(testUser.getId());
    }

    @Test
    void getMyBusiness_UserNotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> businessService.getMyBusiness());

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(businessRepository, never()).findByUserId(any());
    }

    @Test
    void getMyBusiness_BusinessNotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> businessService.getMyBusiness());

        assertEquals("Business not found for user", exception.getMessage());
        verify(businessRepository).findByUserId(testUser.getId());
    }

    @Test
    void getBusinessBySlug_Success() {
        // Given
        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));

        // When
        BusinessResponse response = businessService.getBusinessBySlug(TEST_SLUG);

        // Then
        assertNotNull(response);
        assertEquals(testBusiness.getId(), response.getId());
        assertEquals(testBusiness.getSlug(), response.getSlug());
        assertTrue(response.getIsActive());

        verify(businessRepository).findBySlug(TEST_SLUG);
    }

    @Test
    void getBusinessBySlug_NotFound() {
        // Given
        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> businessService.getBusinessBySlug(TEST_SLUG));

        assertTrue(exception.getMessage().contains("Business not found with slug"));
        verify(businessRepository).findBySlug(TEST_SLUG);
    }

    @Test
    void getBusinessBySlug_NotActive() {
        // Given
        testBusiness.setIsActive(false);
        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));

        // When
        BusinessResponse response = businessService.getBusinessBySlug(TEST_SLUG);

        // Then
        assertNotNull(response);
        assertEquals(testBusiness.getSlug(), response.getSlug());
        assertFalse(response.getIsActive());
        verify(businessRepository).findBySlug(TEST_SLUG);
    }

    @Test
    void updateMyBusiness_Success() {
        // Given
        UpdateBusinessRequest request = UpdateBusinessRequest.builder()
                .businessName("Updated Salon")
                .description("New description")
                .address("456 New St")
                .city("Lyon")
                .postalCode("69001")
                .phone("0987654321")
                .email("new@test.com")
                .category("HEALTH")
                .logoUrl("https://example.com/logo.png")
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(businessRepository.save(any(Business.class))).thenReturn(testBusiness);

        // When
        BusinessResponse response = businessService.updateMyBusiness(request);

        // Then
        assertNotNull(response);
        verify(businessRepository).save(testBusiness);

        // Verify business fields were updated
        assertEquals("Updated Salon", testBusiness.getBusinessName());
        assertEquals("New description", testBusiness.getDescription());
        assertEquals("456 New St", testBusiness.getAddress());
        assertEquals("Lyon", testBusiness.getCity());
        assertEquals("69001", testBusiness.getPostalCode());
        assertEquals("0987654321", testBusiness.getPhone());
        assertEquals("new@test.com", testBusiness.getEmail());
        assertEquals(Business.BusinessCategory.HEALTH, testBusiness.getCategory());
        assertEquals("https://example.com/logo.png", testBusiness.getLogoUrl());
    }

    @Test
    void updateMyBusiness_PartialUpdate() {
        // Given - only update business name
        UpdateBusinessRequest request = UpdateBusinessRequest.builder()
                .businessName("Updated Name Only")
                .build();

        String originalDescription = testBusiness.getDescription();
        String originalAddress = testBusiness.getAddress();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(businessRepository.save(any(Business.class))).thenReturn(testBusiness);

        // When
        BusinessResponse response = businessService.updateMyBusiness(request);

        // Then
        assertNotNull(response);
        assertEquals("Updated Name Only", testBusiness.getBusinessName());
        // Other fields should remain unchanged
        assertEquals(originalDescription, testBusiness.getDescription());
        assertEquals(originalAddress, testBusiness.getAddress());
    }

    @Test
    void updateMyBusiness_UserNotFound() {
        // Given
        UpdateBusinessRequest request = UpdateBusinessRequest.builder()
                .businessName("Updated Salon")
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> businessService.updateMyBusiness(request));

        assertEquals("User not found", exception.getMessage());
        verify(businessRepository, never()).save(any());
    }

    @Test
    void getBusinessServices_Success() {
        // Given
        Service service2 = Service.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .name("Coloring")
                .price(BigDecimal.valueOf(50.00))
                .durationMinutes(60)
                .isActive(true)
                .build();

        List<Service> services = Arrays.asList(testService, service2);

        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByBusinessIdAndIsActiveTrue(testBusiness.getId())).thenReturn(services);

        // When
        List<ServiceResponse> responses = businessService.getBusinessServices(TEST_SLUG);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Haircut", responses.get(0).getName());
        assertEquals("Coloring", responses.get(1).getName());

        verify(businessRepository).findBySlug(TEST_SLUG);
        verify(serviceRepository).findByBusinessIdAndIsActiveTrue(testBusiness.getId());
    }

    @Test
    void getBusinessServices_EmptyList() {
        // Given
        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByBusinessIdAndIsActiveTrue(testBusiness.getId()))
                .thenReturn(Arrays.asList());

        // When
        List<ServiceResponse> responses = businessService.getBusinessServices(TEST_SLUG);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getBusinessServices_BusinessNotActive() {
        // Given
        testBusiness.setIsActive(false);
        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        when(serviceRepository.findByBusinessIdAndIsActiveTrue(testBusiness.getId()))
                .thenReturn(Collections.emptyList());

        // When
        List<ServiceResponse> responses = businessService.getBusinessServices(TEST_SLUG);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(businessRepository).findBySlug(TEST_SLUG);
        verify(serviceRepository).findByBusinessIdAndIsActiveTrue(testBusiness.getId());
    }

    @Test
    void getBusinessServices_BusinessNotFound() {
        // Given
        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> businessService.getBusinessServices(TEST_SLUG));

        assertTrue(exception.getMessage().contains("Business not found with slug"));
        verify(serviceRepository, never()).findByBusinessIdAndIsActiveTrue(any());
    }

    @Test
    void mapToResponse_AllFieldsPresent() {
        // When - using the private method indirectly through getBusinessBySlug
        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));
        BusinessResponse response = businessService.getBusinessBySlug(TEST_SLUG);

        // Then
        assertEquals(testBusiness.getId(), response.getId());
        assertEquals(testBusiness.getBusinessName(), response.getBusinessName());
        assertEquals(testBusiness.getSlug(), response.getSlug());
        assertEquals(testBusiness.getDescription(), response.getDescription());
        assertEquals(testBusiness.getAddress(), response.getAddress());
        assertEquals(testBusiness.getCity(), response.getCity());
        assertEquals(testBusiness.getPostalCode(), response.getPostalCode());
        assertEquals(testBusiness.getPhone(), response.getPhone());
        assertEquals(testBusiness.getEmail(), response.getEmail());
        assertEquals("BEAUTY", response.getCategory());
        assertEquals(testBusiness.getIsActive(), response.getIsActive());
    }

    @Test
    void mapToResponse_NullCategory() {
        // Given
        testBusiness.setCategory(null);
        when(businessRepository.findBySlug(TEST_SLUG)).thenReturn(Optional.of(testBusiness));

        // When
        BusinessResponse response = businessService.getBusinessBySlug(TEST_SLUG);

        // Then
        assertNull(response.getCategory());
    }
}
