package com.booking.api.dto.response;

import com.booking.api.model.BusinessHoliday;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessHolidayResponse {

    private UUID id;
    private UUID businessId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BusinessHolidayResponse fromEntity(BusinessHoliday holiday) {
        return BusinessHolidayResponse.builder()
                .id(holiday.getId())
                .businessId(holiday.getBusiness().getId())
                .startDate(holiday.getStartDate())
                .endDate(holiday.getEndDate())
                .reason(holiday.getReason())
                .createdAt(holiday.getCreatedAt())
                .updatedAt(holiday.getUpdatedAt())
                .build();
    }
}
