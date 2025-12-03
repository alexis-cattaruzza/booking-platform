package com.booking.api.dto.gdpr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for GDPR data export response
 * Contains all personal data for a user (Article 20 - Right to Data Portability)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataExportResponse {

    private String exportDate;
    private String userId;
    private String userType; // BUSINESS or CUSTOMER

    // Personal information
    private PersonalData personalData;

    // Appointments data
    private List<AppointmentData> appointments;

    // Business-specific data (if applicable)
    private BusinessData businessData;

    // Activity logs
    private List<ActivityLog> activityLogs;

    // Notifications
    private List<NotificationData> notifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalData {
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentData {
        private Long id;
        private String serviceName;
        private String businessName;
        private LocalDateTime appointmentDatetime;
        private Integer durationMinutes;
        private Double price;
        private String status;
        private String notes;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessData {
        private String businessName;
        private String siret;
        private String address;
        private String postalCode;
        private String city;
        private String phoneNumber;
        private String description;
        private String slug;
        private List<ServiceData> services;
        private Map<String, String> openingHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceData {
        private Long id;
        private String name;
        private String description;
        private Double price;
        private Integer durationMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLog {
        private LocalDateTime timestamp;
        private String action;
        private String details;
        private String ipAddress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationData {
        private Long id;
        private String type;
        private String channel;
        private String subject;
        private LocalDateTime sentAt;
        private String status;
    }
}
