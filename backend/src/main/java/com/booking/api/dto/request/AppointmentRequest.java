package com.booking.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Valid
    private CustomerRequest customer;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
