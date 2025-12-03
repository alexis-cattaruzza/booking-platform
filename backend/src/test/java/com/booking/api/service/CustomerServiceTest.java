package com.booking.api.service;

import com.booking.api.dto.request.CustomerRequest;
import com.booking.api.dto.response.CustomerResponse;
import com.booking.api.model.Business;
import com.booking.api.model.Customer;
import com.booking.api.model.User;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.CustomerRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService
 * Tests customer CRUD operations, search, and findOrCreateCustomer logic
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomerService customerService;

    private static final String TEST_EMAIL = "owner@test.com";
    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    private User testUser;
    private Business testBusiness;
    private Customer testCustomer;

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

        testCustomer = Customer.builder()
                .id(CUSTOMER_ID)
                .business(testBusiness)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@test.com")
                .phone("0612345678")
                .notes("Regular customer")
                .build();

        // Mock security context
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(TEST_EMAIL);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllCustomers_Success() {
        // Given
        Customer customer2 = Customer.builder()
                .id(UUID.randomUUID())
                .business(testBusiness)
                .firstName("Bob")
                .lastName("Johnson")
                .email("bob@test.com")
                .phone("0687654321")
                .build();

        List<Customer> customers = Arrays.asList(testCustomer, customer2);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findByBusinessId(testBusiness.getId())).thenReturn(customers);

        // When
        List<CustomerResponse> responses = customerService.getAllCustomers();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Jane", responses.get(0).getFirstName());
        assertEquals("Bob", responses.get(1).getFirstName());
        verify(customerRepository).findByBusinessId(testBusiness.getId());
    }

    @Test
    void getAllCustomers_EmptyList() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findByBusinessId(testBusiness.getId())).thenReturn(Collections.emptyList());

        // When
        List<CustomerResponse> responses = customerService.getAllCustomers();

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getAllCustomers_UserNotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.getAllCustomers());

        assertEquals("User not found", exception.getMessage());
        verify(customerRepository, never()).findByBusinessId(any());
    }

    @Test
    void getCustomerById_Success() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findByIdAndBusinessId(CUSTOMER_ID, testBusiness.getId())).thenReturn(Optional.of(testCustomer));

        // When
        CustomerResponse response = customerService.getCustomerById(CUSTOMER_ID);

        // Then
        assertNotNull(response);
        assertEquals(CUSTOMER_ID, response.getId());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("jane@test.com", response.getEmail());
        assertEquals("0612345678", response.getPhone());
    }

    @Test
    void getCustomerById_NotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findByIdAndBusinessId(CUSTOMER_ID, testBusiness.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.getCustomerById(CUSTOMER_ID));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void getCustomerById_NotOwnedByBusiness() {
    // Given
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
    // Repository returns empty because the customer belongs to another business
    when(customerRepository.findByIdAndBusinessId(CUSTOMER_ID, testBusiness.getId()))
            .thenReturn(Optional.empty());

    // When & Then
    RuntimeException exception = assertThrows(RuntimeException.class,
            () -> customerService.getCustomerById(CUSTOMER_ID));

    assertEquals("Customer not found", exception.getMessage());
    }


    @Test
    void findOrCreateCustomer_ExistingCustomer() {
        // Given
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@test.com")
                .phone("0612345678")
                .build();

        when(customerRepository.findByBusinessIdAndEmail(testBusiness.getId(), request.getEmail()))
            .thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class)))
        .thenAnswer(inv -> inv.getArgument(0));

        // When
        Customer result = customerService.findOrCreateCustomer(testBusiness, request);

        // Then
        assertNotNull(result);
        assertEquals(testCustomer.getId(), result.getId());
    }

    @Test
    void findOrCreateCustomer_CreateNewCustomer() {
        // Given
        CustomerRequest request = CustomerRequest.builder()
                .firstName("New")
                .lastName("Customer")
                .email("new@test.com")
                .phone("0698765432")
                .notes("First time visitor")
                .build();

        when(customerRepository.findByBusinessIdAndEmail(testBusiness.getId(), request.getEmail()))
                .thenReturn(Optional.empty());

        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // When
        Customer result = customerService.findOrCreateCustomer(testBusiness, request);

        // Then
        assertNotNull(result);
        assertEquals("New", result.getFirstName());
        assertEquals("Customer", result.getLastName());
        assertEquals("new@test.com", result.getEmail());
        assertEquals("0698765432", result.getPhone());
        assertEquals("First time visitor", result.getNotes());
        assertEquals(testBusiness, result.getBusiness());

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer saved = customerCaptor.getValue();

        assertEquals(testBusiness, saved.getBusiness());
    }


    @Test
    void createCustomer_Success() {
        // Given
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Alice")
                .lastName("Brown")
                .email("alice@test.com")
                .phone("0612349876")
                .notes("VIP customer")
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.existsByBusinessIdAndEmail(testBusiness.getId(), request.getEmail()))
            .thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // When
        CustomerResponse response = customerService.createCustomer(request);

        // Then
        assertNotNull(response);
        assertEquals("Alice", response.getFirstName());
        assertEquals("Brown", response.getLastName());
        assertEquals("alice@test.com", response.getEmail());
        assertEquals("0612349876", response.getPhone());
        assertEquals("VIP customer", response.getNotes());

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer saved = customerCaptor.getValue();
        assertEquals(testBusiness, saved.getBusiness());
    }

    @Test
    void createCustomer_DuplicateEmail() {
        // Given
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Duplicate")
                .email("jane@test.com") // Same as existing customer
                .phone("0698765432")
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.existsByBusinessIdAndEmail(testBusiness.getId(), request.getEmail()))
            .thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.createCustomer(request));

        assertEquals("Customer with this email already exists", exception.getMessage());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateCustomer_Success() {
        // Given
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Jane Updated")
                .lastName("Smith Updated")
                .email("jane.updated@test.com")
                .phone("0687654321")
                .notes("Updated notes")
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findByIdAndBusinessId(CUSTOMER_ID, testBusiness.getId()))
            .thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByBusinessIdAndEmail(testBusiness.getId(), request.getEmail()))
                .thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CustomerResponse response = customerService.updateCustomer(CUSTOMER_ID, request);

        // Then
        assertNotNull(response);
        assertEquals("Jane Updated", testCustomer.getFirstName());
        assertEquals("Smith Updated", testCustomer.getLastName());
        assertEquals("jane.updated@test.com", testCustomer.getEmail());
        assertEquals("0687654321", testCustomer.getPhone());
        assertEquals("Updated notes", testCustomer.getNotes());
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void updateCustomer_EmailAlreadyExists() {
        // Given

        CustomerRequest request = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("other@test.com") // Try to update to existing email
                .phone("0612345678")
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findByIdAndBusinessId(CUSTOMER_ID, testBusiness.getId()))
            .thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByBusinessIdAndEmail(testBusiness.getId(), request.getEmail()))
                .thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.updateCustomer(CUSTOMER_ID, request));

        assertEquals("Customer with this email already exists", exception.getMessage());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateCustomer_SameEmailIsAllowed() {
        // Given - Updating with same email should be allowed
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Jane Updated")
                .lastName("Smith Updated")
                .email("jane@test.com") // Same email
                .phone("0612345678")
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findByIdAndBusinessId(CUSTOMER_ID, testBusiness.getId()))
                .thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CustomerResponse response = customerService.updateCustomer(CUSTOMER_ID, request);

        // Then
        assertNotNull(response);
        assertEquals("Jane Updated", testCustomer.getFirstName());
        verify(customerRepository).save(testCustomer);
    }


    @Test
    void updateCustomer_NotFound() {
        // Given
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Test")
                .lastName("Test")
                .email("test@test.com")
                .phone("0612345678")
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findByIdAndBusinessId(CUSTOMER_ID, testBusiness.getId()))
            .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.updateCustomer(CUSTOMER_ID, request));

        assertEquals("Customer not found", exception.getMessage());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void deleteCustomer_Success() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findByIdAndBusinessId(CUSTOMER_ID, testBusiness.getId()))
            .thenReturn(Optional.of(testCustomer));

        // When
        customerService.deleteCustomer(CUSTOMER_ID);

        // Then
        verify(customerRepository).delete(testCustomer);
    }

    @Test
    void deleteCustomer_NotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findByIdAndBusinessId(CUSTOMER_ID, testBusiness.getId()))
            .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.deleteCustomer(CUSTOMER_ID));

        assertEquals("Customer not found", exception.getMessage());
        verify(customerRepository, never()).delete(any());
    }

    @Test
    void searchCustomers_Success() {
        // Given
        String query = "jane";
        String expectedQuery = "%jane%";
        List<Customer> foundCustomers = Arrays.asList(testCustomer);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.searchCustomers(testBusiness.getId(), expectedQuery))
            .thenReturn(foundCustomers);

        // When
        List<CustomerResponse> responses = customerService.searchCustomers(query);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Jane", responses.get(0).getFirstName());
        verify(customerRepository).searchCustomers(testBusiness.getId(), expectedQuery);
    }

    @Test
    void searchCustomers_EmptyResult() {
        // Given
        String query = "nonexistent";
        String expectedQuery = "%nonexistent%";

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.searchCustomers(testBusiness.getId(), expectedQuery)).thenReturn(Collections.emptyList());
        // When
        List<CustomerResponse> responses = customerService.searchCustomers(query);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void searchCustomers_CaseInsensitive() {
        // Given
        String query = "JANE"; // Uppercase query
        String expectedQuery = "%jane%";
        List<Customer> foundCustomers = Arrays.asList(testCustomer);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(businessRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testBusiness));
        when(customerRepository.searchCustomers(testBusiness.getId(), expectedQuery)).thenReturn(foundCustomers);

        // When
        List<CustomerResponse> responses = customerService.searchCustomers(query);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }
}
