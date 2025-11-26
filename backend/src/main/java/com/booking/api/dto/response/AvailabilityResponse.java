package com.booking.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityResponse {

    private LocalDate date;
    private List<TimeSlot> availableSlots;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeSlot {
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean available;
    }
}
