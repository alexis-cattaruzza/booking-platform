package com.booking.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponse {
    private UUID id;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    private Boolean isActive;
}
