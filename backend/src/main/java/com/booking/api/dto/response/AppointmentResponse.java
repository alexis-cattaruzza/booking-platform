package com.booking.api.dto.response;

import com.booking.api.model.Appointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponse {

    private UUID id;
    private LocalDateTime appointmentDatetime;
    private Integer durationMinutes;
    private BigDecimal price;
    private Appointment.AppointmentStatus status;
    private String notes;
    private String cancellationToken;

    // Service info
    private ServiceInfo service;

    // Customer info
    private CustomerInfo customer;

    private LocalDateTime createdAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServiceInfo {
        private UUID id;
        private String name;
        private Integer durationMinutes;
        private BigDecimal price;
        private String color;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CustomerInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
    }
}
