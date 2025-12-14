package com.booking.api.service;

import com.booking.api.dto.gdpr.AccountDeletionRequest;
import com.booking.api.dto.gdpr.AccountDeletionResponse;
import com.booking.api.dto.gdpr.DataExportResponse;
import com.booking.api.exception.BadRequestException;
import com.booking.api.model.*;
import com.booking.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GdprService {

    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AppointmentService appointmentService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Transactional(readOnly = true)
    public DataExportResponse exportUserData(String email, String userType) {
        if (userType == null || userType.isBlank()) {
            throw new BadRequestException("userType is required");
        }

        if (!userType.equalsIgnoreCase("business") && !userType.equalsIgnoreCase("customer")) {
            throw new BadRequestException("Invalid userType");
        }
        
        if ("BUSINESS".equalsIgnoreCase(userType)) {
            return exportBusinessData(email);
        }
        return exportCustomerData(email);
    }

    private DataExportResponse exportBusinessData(String email) {
        Business business = businessRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        List<Appointment> appointments = appointmentRepository
                .findByBusinessIdOrderByAppointmentDatetimeDesc(business.getId());
        List<com.booking.api.model.Service> services = serviceRepository
                .findByBusinessIdOrderByNameAsc(business.getId());

        return DataExportResponse.builder()
                .exportDate(LocalDateTime.now().format(DATE_FORMATTER))
                .userId(business.getId().toString())
                .userType("BUSINESS")
                .personalData(buildPersonalData(business.getUser(), business.getPhone(), business.getCreatedAt()))
                .appointments(appointments.stream().map(this::mapAppointment).collect(Collectors.toList()))
                .businessData(buildBusinessData(business, services))
                .notifications(List.of())
                .activityLogs(List.of())
                .build();
    }

    private DataExportResponse exportCustomerData(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        List<Appointment> appointments = appointmentRepository
                .findByCustomerIdOrderByAppointmentDatetimeDesc(customer.getId());

        return DataExportResponse.builder()
                .exportDate(LocalDateTime.now().format(DATE_FORMATTER))
                .userId(customer.getId().toString())
                .userType("CUSTOMER")
                .personalData(buildPersonalData(customer))
                .appointments(appointments.stream().map(this::mapAppointment).collect(Collectors.toList()))
                .businessData(null)
                .notifications(List.of())
                .activityLogs(List.of())
                .build();
    }

    @Transactional
    public AccountDeletionResponse deleteUserAccount(String email, String userType,
                                                      AccountDeletionRequest request) {
        if (userType == null || userType.isBlank()) {
            throw new BadRequestException("userType is required");
        }

        if (!userType.equalsIgnoreCase("business") && !userType.equalsIgnoreCase("customer")) {
            throw new BadRequestException("Invalid userType");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        if ("BUSINESS".equalsIgnoreCase(userType)) {
            Business business = businessRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Business not found"));
            return deleteBusinessAccount(business, user);
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return deleteCustomerAccount(customer);
    }

    private AccountDeletionResponse deleteBusinessAccount(Business business, User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime effectiveDate = now.plusDays(30);

        // Get future appointments count
        List<Appointment> futureAppointments = appointmentRepository
                .findByBusinessIdAndAppointmentDatetimeAfter(business.getId(), now);
        int futureAppointmentsCount = futureAppointments.size();

        // Mark business for deletion FIRST
        // This allows the user to continue logging in and cancel the deletion
        business.setDeletedAt(now);
        businessRepository.save(business);

        log.info("Business marked for deletion: {} (effective: {}). {} appointments to cancel. User can still login during grace period.",
                business.getBusinessName(), effectiveDate, futureAppointmentsCount);

        return AccountDeletionResponse.builder()
                .message("Votre compte sera supprimé dans 30 jours. Vous pouvez encore vous connecter et annuler cette demande.")
                .deletionDate(now)
                .effectiveDate(effectiveDate)
                .canRecover(true)
                .futureAppointmentsCount(futureAppointmentsCount)
                .build();
    }

    /**
     * Cancel all future appointments for a business marked for deletion
     * This method should be called AFTER the main transaction commits
     */
    public void cancelBusinessAppointments(UUID businessId) {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> futureAppointments = appointmentRepository
                .findByBusinessIdAndAppointmentDatetimeAfter(businessId, now);

        log.info("Cancelling {} future appointments for business {}", futureAppointments.size(), businessId);

        // Cancel each appointment using the service (which handles status, email, etc.)
        for (Appointment apt : futureAppointments) {
            // Skip if already cancelled or completed
            if (apt.getStatus() == Appointment.AppointmentStatus.CANCELLED ||
                apt.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
                log.debug("Skipping appointment {} - already {}", apt.getId(), apt.getStatus());
                continue;
            }

            try {
                appointmentService.cancelAppointmentByBusiness(
                    apt.getId(),
                    "Fermeture définitive du compte business - Tous les rendez-vous ont été annulés",
                    businessId
                );
                log.debug("Cancelled appointment {} for business deletion", apt.getId());
            } catch (Exception e) {
                log.error("Failed to cancel appointment {}: {}", apt.getId(), e.getMessage(), e);
            }
        }

        log.info("Appointment cancellation completed for business {}", businessId);
    }

    private AccountDeletionResponse deleteCustomerAccount(Customer customer) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime effectiveDate = now.plusDays(30);

        List<Appointment> futureAppointments = appointmentRepository
                .findByCustomerIdAndAppointmentDatetimeAfter(customer.getId(), now);

        for (Appointment apt : futureAppointments) {
            apt.setStatus(Appointment.AppointmentStatus.CANCELLED);
            appointmentRepository.save(apt);
        }

        customer.setDeletedAt(now);
        customer.setEmail(customer.getEmail() + ".deleted." + System.currentTimeMillis());
        customerRepository.save(customer);

        log.info("Customer marked for deletion: {} {}", customer.getFirstName(), customer.getLastName());

        return AccountDeletionResponse.builder()
                .message("Votre compte sera supprimé dans 30 jours. Vous pouvez annuler cette demande.")
                .deletionDate(now)
                .effectiveDate(effectiveDate)
                .canRecover(true)
                .build();
    }

    private DataExportResponse.PersonalData buildPersonalData(User user, String phone, LocalDateTime createdAt) {
        return DataExportResponse.PersonalData.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(phone)
                .createdAt(createdAt)
                .lastLoginAt(null)
                .build();
    }

    private DataExportResponse.PersonalData buildPersonalData(Customer customer) {
        return DataExportResponse.PersonalData.builder()
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhone())
                .createdAt(customer.getCreatedAt())
                .lastLoginAt(null)
                .build();
    }

    private DataExportResponse.BusinessData buildBusinessData(Business business,
                                                               List<com.booking.api.model.Service> services) {
        return DataExportResponse.BusinessData.builder()
                .businessName(business.getBusinessName())
                .siret(null)
                .address(business.getAddress())
                .postalCode(business.getPostalCode())
                .city(business.getCity())
                .phoneNumber(business.getPhone())
                .description(business.getDescription())
                .slug(business.getSlug())
                .services(services.stream().map(this::mapService).collect(Collectors.toList()))
                .openingHours(Map.of())
                .build();
    }

    private DataExportResponse.AppointmentData mapAppointment(Appointment apt) {
        return DataExportResponse.AppointmentData.builder()
                .id(apt.getId().getMostSignificantBits())
                .serviceName(apt.getService().getName())
                .businessName(apt.getBusiness().getBusinessName())
                .appointmentDatetime(apt.getAppointmentDatetime())
                .durationMinutes(apt.getDurationMinutes())
                .price(apt.getPrice() != null ? apt.getPrice().doubleValue() : null)
                .status(apt.getStatus().toString())
                .notes(apt.getNotes())
                .createdAt(apt.getCreatedAt())
                .build();
    }

    private DataExportResponse.ServiceData mapService(com.booking.api.model.Service service) {
        return DataExportResponse.ServiceData.builder()
                .id(service.getId().getMostSignificantBits())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice() != null ? service.getPrice().doubleValue() : null)
                .durationMinutes(service.getDurationMinutes())
                .build();
    }

    /**
     * Count future appointments between two dates
     */
    public int countFutureAppointments(java.util.UUID businessId, LocalDateTime start, LocalDateTime end) {
        List<Appointment> appointments = appointmentRepository
                .findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(businessId, start, end);
        return appointments.size();
    }

    /**
     * Send deletion confirmation email to business (call this AFTER the transaction commits)
     */
    public void sendBusinessDeletionEmail(String email, int futureAppointmentsCount) {
        Business business = businessRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        LocalDateTime effectiveDate = business.getDeletedAt().plusDays(30);

        // Send deletion confirmation email to business
        try {
            emailService.sendBusinessDeletionRequestEmail(
                business.getEmail(),
                business.getBusinessName(),
                effectiveDate,
                futureAppointmentsCount
            );
            log.info("Business deletion confirmation email sent to: {}", business.getBusinessName());
        } catch (Exception e) {
            log.error("Failed to send deletion confirmation email: {}", e.getMessage());
        }
    }

    /**
     * Cancel deletion request - restore business account
     */
    @Transactional
    public void cancelDeletion(String email) {
        log.info("Cancelling deletion request for: {}", email);

        Business business = businessRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        if (business.getDeletedAt() == null) {
            throw new BadRequestException("No deletion request found for this business");
        }

        // Restore business - clear deletion date
        business.setDeletedAt(null);
        businessRepository.save(business);

        log.info("Deletion cancelled successfully for business: {}", business.getBusinessName());

        // Send cancellation confirmation email to business
        emailService.sendBusinessDeletionCancellationEmail(
            business.getEmail(),
            business.getBusinessName()
        );
    }
}
