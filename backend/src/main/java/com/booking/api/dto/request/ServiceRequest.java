package com.booking.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequest {

    @NotBlank(message = "Service name is required")
    @Size(max = 255, message = "Service name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Duration is required")
    @Min(value = 5, message = "Duration must be at least 5 minutes")
    @Max(value = 480, message = "Duration must not exceed 8 hours (480 minutes)")
    private Integer durationMinutes;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimal places")
    private BigDecimal price;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color (e.g., #3b82f6)")
    private String color;

    private Boolean isActive;

    private Integer displayOrder;
}
