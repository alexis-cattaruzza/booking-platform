package com.booking.api.dto.gdpr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for account deletion response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletionResponse {

    private String message;
    private LocalDateTime deletionDate;
    private LocalDateTime effectiveDate; // When data will be permanently deleted (30 days grace period)
    private Boolean canRecover; // Whether account can be recovered within grace period
    private Integer futureAppointmentsCount; // Number of future appointments that were cancelled
}
