package com.booking.api.controller;

import com.booking.api.dto.request.CustomerRequest;
import com.booking.api.dto.response.CustomerResponse;
import com.booking.api.service.CustomerService;
import com.booking.api.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour CustomerController
 * Test des endpoints de gestion des clients
 */
@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private com.booking.api.service.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private com.booking.api.service.AuditService auditService;

    private CustomerRequest customerRequest;
    private CustomerResponse customerResponse;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        customerRequest = CustomerRequest.builder()
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .phone("+33612345678")
                .notes("Client régulier")
                .build();

        customerResponse = CustomerResponse.builder()
                .id(customerId)
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .phone("+33612345678")
                .notes("Client régulier")
                .createdAt(LocalDateTime.now())
                .lastVisit(LocalDateTime.now())
                .build();
    }

    // ==================== GET /api/customers ====================

    @Test
    void getAllCustomers_Success() throws Exception {
        // Given
        CustomerResponse customer2 = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Marie")
                .lastName("Martin")
                .email("marie.martin@example.com")
                .phone("+33623456789")
                .createdAt(LocalDateTime.now())
                .build();

        List<CustomerResponse> customers = Arrays.asList(customerResponse, customer2);
        when(customerService.getAllCustomers()).thenReturn(customers);

        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("Jean"))
                .andExpect(jsonPath("$[0].lastName").value("Dupont"))
                .andExpect(jsonPath("$[0].email").value("jean.dupont@example.com"))
                .andExpect(jsonPath("$[1].firstName").value("Marie"))
                .andExpect(jsonPath("$[1].lastName").value("Martin"));

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    void getAllCustomers_EmptyList() throws Exception {
        // Given
        when(customerService.getAllCustomers()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    void getAllCustomers_ServiceError() throws Exception {
        // Given
        when(customerService.getAllCustomers())
                .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isInternalServerError());

        verify(customerService, times(1)).getAllCustomers();
    }

    // ==================== GET /api/customers/{id} ====================

    @Test
    void getCustomerById_Success() throws Exception {
        // Given
        when(customerService.getCustomerById(customerId)).thenReturn(customerResponse);

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.email").value("jean.dupont@example.com"))
                .andExpect(jsonPath("$.phone").value("+33612345678"))
                .andExpect(jsonPath("$.notes").value("Client régulier"));

        verify(customerService, times(1)).getCustomerById(customerId);
    }

    @Test
    void getCustomerById_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(customerService.getCustomerById(nonExistentId))
                .thenThrow(new RuntimeException("Customer not found"));

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", nonExistentId))
                .andExpect(status().isInternalServerError());

        verify(customerService, times(1)).getCustomerById(nonExistentId);
    }

    @Test
    void getCustomerById_InvalidUUID() throws Exception {
        // When & Then
        // Note: GlobalExceptionHandler now catches MethodArgumentTypeMismatchException → 400
        mockMvc.perform(get("/api/customers/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).getCustomerById(any());
    }

    // ==================== GET /api/customers/search ====================

    @Test
    void searchCustomers_WithResults() throws Exception {
        // Given
        String query = "Jean";
        List<CustomerResponse> searchResults = Arrays.asList(customerResponse);
        when(customerService.searchCustomers(query)).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/customers/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Jean"))
                .andExpect(jsonPath("$[0].lastName").value("Dupont"));

        verify(customerService, times(1)).searchCustomers(query);
    }

    @Test
    void searchCustomers_NoResults() throws Exception {
        // Given
        String query = "NonExistent";
        when(customerService.searchCustomers(query)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/customers/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(customerService, times(1)).searchCustomers(query);
    }

    @Test
    void searchCustomers_PartialMatch() throws Exception {
        // Given
        String query = "Dup";
        CustomerResponse customer2 = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Pierre")
                .lastName("Dupuis")
                .email("pierre.dupuis@example.com")
                .phone("0634567890")
                .createdAt(LocalDateTime.now())
                .build();

        List<CustomerResponse> searchResults = Arrays.asList(customerResponse, customer2);
        when(customerService.searchCustomers(query)).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/customers/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].lastName").value("Dupont"))
                .andExpect(jsonPath("$[1].lastName").value("Dupuis"));

        verify(customerService, times(1)).searchCustomers(query);
    }

    @Test
    void searchCustomers_EmptyQuery() throws Exception {
        // Given
        String query = "";
        when(customerService.searchCustomers(query)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/customers/search")
                        .param("query", query))
                .andExpect(status().isOk());

        verify(customerService, times(1)).searchCustomers(query);
    }

    @Test
    void searchCustomers_EmailSearch() throws Exception {
        // Given
        String query = "jean.dupont@example.com";
        List<CustomerResponse> searchResults = Arrays.asList(customerResponse);
        when(customerService.searchCustomers(query)).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/customers/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value(query));

        verify(customerService, times(1)).searchCustomers(query);
    }

    @Test
    void searchCustomers_PhoneSearch() throws Exception {
        // Given
        String query = "+33612345678";
        List<CustomerResponse> searchResults = Arrays.asList(customerResponse);
        when(customerService.searchCustomers(query)).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/customers/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].phone").value(query));

        verify(customerService, times(1)).searchCustomers(query);
    }

    // ==================== POST /api/customers ====================

    @Test
    void createCustomer_Success() throws Exception {
        // Given
        when(customerService.createCustomer(any(CustomerRequest.class)))
                .thenReturn(customerResponse);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.email").value("jean.dupont@example.com"))
                .andExpect(jsonPath("$.phone").value("+33612345678"));

        verify(customerService, times(1)).createCustomer(any(CustomerRequest.class));
    }

    @Test
    void createCustomer_WithInternationalPhone() throws Exception {
        // Given
        CustomerRequest requestWithIntlPhone = CustomerRequest.builder()
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .phone("+33612345678")
                .build();

        CustomerResponse responseWithIntlPhone = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .phone("+33612345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(customerService.createCustomer(any(CustomerRequest.class)))
                .thenReturn(responseWithIntlPhone);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithIntlPhone)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").value("+33612345678"));

        verify(customerService, times(1)).createCustomer(any(CustomerRequest.class));
    }

    @Test
    void createCustomer_MissingFirstName() throws Exception {
        // Given
        customerRequest.setFirstName(null);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_EmptyFirstName() throws Exception {
        // Given
        customerRequest.setFirstName("");

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_MissingLastName() throws Exception {
        // Given
        customerRequest.setLastName(null);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_EmptyLastName() throws Exception {
        // Given
        customerRequest.setLastName("");

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_InvalidEmail() throws Exception {
        // Given
        customerRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_MissingEmail() throws Exception {
        // Given
        customerRequest.setEmail(null);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_EmptyEmail() throws Exception {
        // Given
        customerRequest.setEmail("");

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_InvalidPhoneFormat() throws Exception {
        // Given
        customerRequest.setPhone("123456");

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_MissingPhone() throws Exception {
        // Given
        customerRequest.setPhone(null);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_EmptyPhone() throws Exception {
        // Given
        customerRequest.setPhone("");

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_PhoneWithInvalidPrefix() throws Exception {
        // Given
        customerRequest.setPhone("0012345678"); // Invalid prefix

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_FirstNameTooLong() throws Exception {
        // Given
        customerRequest.setFirstName("A".repeat(101)); // Max is 100

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_LastNameTooLong() throws Exception {
        // Given
        customerRequest.setLastName("B".repeat(101)); // Max is 100

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_EmailTooLong() throws Exception {
        // Given
        String longEmail = "a".repeat(240) + "@example.com"; // Max is 255
        customerRequest.setEmail(longEmail);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_NotesTooLong() throws Exception {
        // Given
        customerRequest.setNotes("N".repeat(501)); // Max is 500

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_WithoutNotes() throws Exception {
        // Given
        customerRequest.setNotes(null);
        CustomerResponse responseWithoutNotes = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .phone("0612345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(customerService.createCustomer(any(CustomerRequest.class)))
                .thenReturn(responseWithoutNotes);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notes").isEmpty());

        verify(customerService, times(1)).createCustomer(any(CustomerRequest.class));
    }

    @Test
    void createCustomer_DuplicateEmail() throws Exception {
        // Given
        when(customerService.createCustomer(any(CustomerRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isInternalServerError());

        verify(customerService, times(1)).createCustomer(any(CustomerRequest.class));
    }

    // ==================== PUT /api/customers/{id} ====================

    @Test
    void updateCustomer_Success() throws Exception {
        // Given
        CustomerResponse updatedResponse = CustomerResponse.builder()
                .id(customerId)
                .firstName("Jean-Pierre")
                .lastName("Dupont-Martin")
                .email("jp.dupont@example.com")
                .phone("0698765432")
                .notes("Notes mises à jour")
                .createdAt(LocalDateTime.now())
                .lastVisit(LocalDateTime.now())
                .build();

        when(customerService.updateCustomer(eq(customerId), any(CustomerRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.firstName").value("Jean-Pierre"))
                .andExpect(jsonPath("$.lastName").value("Dupont-Martin"))
                .andExpect(jsonPath("$.email").value("jp.dupont@example.com"));

        verify(customerService, times(1)).updateCustomer(eq(customerId), any(CustomerRequest.class));
    }

    @Test
    void updateCustomer_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(customerService.updateCustomer(eq(nonExistentId), any(CustomerRequest.class)))
                .thenThrow(new RuntimeException("Customer not found"));

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isInternalServerError());

        verify(customerService, times(1)).updateCustomer(eq(nonExistentId), any(CustomerRequest.class));
    }

    @Test
    void updateCustomer_InvalidEmail() throws Exception {
        // Given
        customerRequest.setEmail("invalid-email-format");

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).updateCustomer(any(), any());
    }

    @Test
    void updateCustomer_InvalidPhone() throws Exception {
        // Given
        customerRequest.setPhone("123");

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).updateCustomer(any(), any());
    }

    @Test
    void updateCustomer_MissingRequiredFields() throws Exception {
        // Given
        customerRequest.setFirstName(null);
        customerRequest.setLastName(null);

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).updateCustomer(any(), any());
    }

    @Test
    void updateCustomer_InvalidUUID() throws Exception {
        // When & Then
        // Note: GlobalExceptionHandler now catches MethodArgumentTypeMismatchException → 400
        mockMvc.perform(put("/api/customers/{id}", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).updateCustomer(any(), any());
    }

    // ==================== DELETE /api/customers/{id} ====================

    @Test
    void deleteCustomer_Success() throws Exception {
        // Given
        doNothing().when(customerService).deleteCustomer(customerId);

        // When & Then
        mockMvc.perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer(customerId);
    }

    @Test
    void deleteCustomer_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new RuntimeException("Customer not found"))
                .when(customerService).deleteCustomer(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/customers/{id}", nonExistentId))
                .andExpect(status().isInternalServerError());

        verify(customerService, times(1)).deleteCustomer(nonExistentId);
    }

    @Test
    void deleteCustomer_InvalidUUID() throws Exception {
        // When & Then
        // Note: GlobalExceptionHandler now catches MethodArgumentTypeMismatchException → 400
        mockMvc.perform(delete("/api/customers/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).deleteCustomer(any());
    }

    @Test
    void deleteCustomer_WithActiveBookings() throws Exception {
        // Given
        doThrow(new RuntimeException("Cannot delete customer with active bookings"))
                .when(customerService).deleteCustomer(customerId);

        // When & Then
        mockMvc.perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isInternalServerError());

        verify(customerService, times(1)).deleteCustomer(customerId);
    }

    // ==================== Edge Cases and Additional Scenarios ====================

    @Test
    void createCustomer_WithSpecialCharactersInName() throws Exception {
        // Given
        CustomerRequest specialCharsRequest = CustomerRequest.builder()
                .firstName("Jean-François")
                .lastName("O'Connor")
                .email("jf.oconnor@example.com")
                .phone("+33612345678")
                .build();

        CustomerResponse specialCharsResponse = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Jean-François")
                .lastName("O'Connor")
                .email("jf.oconnor@example.com")
                .phone("+33612345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(customerService.createCustomer(any(CustomerRequest.class)))
                .thenReturn(specialCharsResponse);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialCharsRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jean-François"))
                .andExpect(jsonPath("$.lastName").value("O'Connor"));

        verify(customerService, times(1)).createCustomer(any(CustomerRequest.class));
    }

    @Test
    void searchCustomers_CaseInsensitive() throws Exception {
        // Given
        String query = "DUPONT";
        List<CustomerResponse> searchResults = Arrays.asList(customerResponse);
        when(customerService.searchCustomers(query)).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/customers/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(customerService, times(1)).searchCustomers(query);
    }

    @Test
    void createCustomer_WithMaxLengthFields() throws Exception {
        // Given
        // Using reasonable max length values that still pass validation
        // Email validators typically have practical limits (64 char local part per RFC)
        String longEmail = "very.long.email.address.that.is.still.valid@example-domain.com";

        CustomerRequest maxLengthRequest = CustomerRequest.builder()
                .firstName("A".repeat(100))
                .lastName("B".repeat(100))
                .email(longEmail)
                .phone("+33612345678")
                .notes("N".repeat(500))
                .build();

        CustomerResponse maxLengthResponse = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("A".repeat(100))
                .lastName("B".repeat(100))
                .email(longEmail)
                .phone("+33612345678")
                .notes("N".repeat(500))
                .createdAt(LocalDateTime.now())
                .build();

        when(customerService.createCustomer(any(CustomerRequest.class)))
                .thenReturn(maxLengthResponse);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maxLengthRequest)))
                .andExpect(status().isCreated());

        verify(customerService, times(1)).createCustomer(any(CustomerRequest.class));
    }

    @Test
    void createCustomer_WithEmptyNotes() throws Exception {
        // Given
        customerRequest.setNotes("");
        CustomerResponse responseWithEmptyNotes = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .phone("0612345678")
                .notes("")
                .createdAt(LocalDateTime.now())
                .build();

        when(customerService.createCustomer(any(CustomerRequest.class)))
                .thenReturn(responseWithEmptyNotes);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notes").value(""));

        verify(customerService, times(1)).createCustomer(any(CustomerRequest.class));
    }

    @Test
    void getAllCustomers_LargeDataset() throws Exception {
        // Given
        List<CustomerResponse> largeList = Arrays.asList(
                customerResponse,
                CustomerResponse.builder().id(UUID.randomUUID()).firstName("Test1").lastName("User1")
                        .email("test1@example.com").phone("0612345679").createdAt(LocalDateTime.now()).build(),
                CustomerResponse.builder().id(UUID.randomUUID()).firstName("Test2").lastName("User2")
                        .email("test2@example.com").phone("0612345680").createdAt(LocalDateTime.now()).build()
        );
        when(customerService.getAllCustomers()).thenReturn(largeList);

        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    void createCustomer_WithAllValidFrenchPhoneFormats() throws Exception {
        // Test various valid international phone formats
        String[] validPhones = {
                "+33612345678",
                "+33712345678",
                "+33812345678",
                "+33912345678",
                "+14155552671",  // US format
                "+442071838750"  // UK format
        };

        for (String phone : validPhones) {
            customerRequest.setPhone(phone);
            CustomerResponse response = CustomerResponse.builder()
                    .id(UUID.randomUUID())
                    .firstName("Jean")
                    .lastName("Dupont")
                    .email("jean.dupont@example.com")
                    .phone(phone)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(customerService.createCustomer(any(CustomerRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.phone").value(phone));

            reset(customerService);
        }
    }
}
