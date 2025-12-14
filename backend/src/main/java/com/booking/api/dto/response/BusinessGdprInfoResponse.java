package com.booking.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for business GDPR information
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessGdprInfoResponse {
    private String businessName;
    private String email;
    private String createdAt;
    private String deletedAt; // When deletion was requested (null if not requested)
    private String effectiveDeletionDate; // When account will be permanently deleted
    private Integer deletionGracePeriodDays;
    private Integer futureAppointmentsCount; // Number of appointments in next 30 days
    private List<String> dataCategories;
}
