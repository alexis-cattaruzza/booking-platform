package com.booking.api.dto.gdpr;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for account deletion request
 * GDPR Article 17 - Right to be Forgotten
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletionRequest {

    @NotBlank(message = "Password confirmation is required")
    private String password;

    private String reason; // Optional: why user is deleting account

    private Boolean confirmDeletion; // User must confirm they understand data will be deleted
}
