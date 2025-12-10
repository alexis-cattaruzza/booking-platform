package com.booking.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * GDPR: Response containing all user data for export
 * Article 20 - Right to data portability
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDataExportResponse {

    // Personal Information
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDateTime createdAt;
    private Boolean emailVerified;

    // Business Information
    private BusinessData business;

    // Appointments
    private List<AppointmentData> appointments;

    // Audit Logs (user's own actions)
    private List<AuditData> activityHistory;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BusinessData {
        private String businessName;
        private String description;
        private String address;
        private String city;
        private String postalCode;
        private String category;
        private String slug;
        private List<ServiceData> services;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServiceData {
        private String name;
        private String description;
        private Integer durationMinutes;
        private Double price;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AppointmentData {
        private LocalDateTime appointmentDatetime;
        private String serviceName;
        private String customerName;
        private String customerEmail;
        private String status;
        private String notes;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuditData {
        private String action;
        private LocalDateTime timestamp;
        private String ipAddress;
        private String status;
    }
}
