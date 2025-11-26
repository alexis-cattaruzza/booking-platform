package com.booking.api.service;

import com.booking.api.dto.request.CustomerRequest;
import com.booking.api.dto.response.CustomerResponse;
import com.booking.api.model.Business;
import com.booking.api.model.Customer;
import com.booking.api.model.User;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.CustomerRepository;
import com.booking.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        Business business = getAuthenticatedUserBusiness();
        log.info("Retrieving all customers for business: {}", business.getId());

        return customerRepository.findByBusinessId(business.getId()).stream()
                .map(this::toCustomerResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID customerId) {
        Business business = getAuthenticatedUserBusiness();
        log.info("Retrieving customer {} for business: {}", customerId, business.getId());

        Customer customer = customerRepository.findByIdAndBusinessId(customerId, business.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return toCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public Customer findOrCreateCustomer(Business business, CustomerRequest request) {
        log.info("Finding or creating customer with email: {} for business: {}",
                request.getEmail(), business.getId());

        // Try to find existing customer by email
        return customerRepository.findByBusinessIdAndEmail(business.getId(), request.getEmail())
                .map(existing -> {
                    // Update existing customer info
                    existing.setFirstName(request.getFirstName());
                    existing.setLastName(request.getLastName());
                    existing.setPhone(request.getPhone());
                    if (request.getNotes() != null) {
                        existing.setNotes(request.getNotes());
                    }
                    return customerRepository.save(existing);
                })
                .orElseGet(() -> {
                    // Create new customer
                    Customer newCustomer = Customer.builder()
                            .business(business)
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .email(request.getEmail())
                            .phone(request.getPhone())
                            .notes(request.getNotes())
                            .build();
                    return customerRepository.save(newCustomer);
                });
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        Business business = getAuthenticatedUserBusiness();
        log.info("Creating new customer for business: {}", business.getId());

        // Check if customer already exists
        if (customerRepository.existsByBusinessIdAndEmail(business.getId(), request.getEmail())) {
            throw new RuntimeException("Customer with this email already exists");
        }

        Customer customer = Customer.builder()
                .business(business)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .notes(request.getNotes())
                .build();

        customer = customerRepository.save(customer);
        log.info("Customer created with ID: {}", customer.getId());

        return toCustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID customerId, CustomerRequest request) {
        Business business = getAuthenticatedUserBusiness();
        log.info("Updating customer {} for business: {}", customerId, business.getId());

        Customer customer = customerRepository.findByIdAndBusinessId(customerId, business.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Check if email is being changed and if it's already used
        if (!customer.getEmail().equals(request.getEmail()) &&
                customerRepository.existsByBusinessIdAndEmail(business.getId(), request.getEmail())) {
            throw new RuntimeException("Customer with this email already exists");
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setNotes(request.getNotes());

        customer = customerRepository.save(customer);
        log.info("Customer updated: {}", customer.getId());

        return toCustomerResponse(customer);
    }

    @Transactional
    public void deleteCustomer(UUID customerId) {
        Business business = getAuthenticatedUserBusiness();
        log.info("Deleting customer {} for business: {}", customerId, business.getId());

        Customer customer = customerRepository.findByIdAndBusinessId(customerId, business.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customerRepository.delete(customer);
        log.info("Customer deleted: {}", customerId);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> searchCustomers(String query) {
        Business business = getAuthenticatedUserBusiness();
        log.info("Searching customers for business: {} with query: {}", business.getId(), query);

        String searchQuery = "%" + query.toLowerCase() + "%";
        return customerRepository.searchCustomers(business.getId(), searchQuery).stream()
                .map(this::toCustomerResponse)
                .collect(Collectors.toList());
    }

    private Business getAuthenticatedUserBusiness() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return businessRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Business not found for user"));
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .notes(customer.getNotes())
                .createdAt(customer.getCreatedAt())
                .lastVisit(customer.getLastAppointmentAt())
                .build();
    }
}
