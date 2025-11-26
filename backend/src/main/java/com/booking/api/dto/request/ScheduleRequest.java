package com.booking.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleRequest {

    @NotNull(message = "Day of week is required")
    @Pattern(regexp = "^(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)$",
            message = "Day of week must be a valid day (MONDAY, TUESDAY, etc.)")
    private String dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Min(value = 5, message = "Slot duration must be at least 5 minutes")
    @Max(value = 240, message = "Slot duration must not exceed 4 hours (240 minutes)")
    private Integer slotDurationMinutes;

    private Boolean isActive;
}
