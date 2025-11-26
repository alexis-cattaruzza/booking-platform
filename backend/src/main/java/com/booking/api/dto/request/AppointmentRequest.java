package com.booking.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequest {

    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    @NotNull(message = "Appointment date and time is required")
    private LocalDateTime appointmentDatetime;

    @NotNull(message = "Customer information is required")
    private CustomerRequest customer;

    private String notes;
}
