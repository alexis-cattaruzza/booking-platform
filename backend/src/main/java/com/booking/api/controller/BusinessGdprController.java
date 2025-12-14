package com.booking.api.controller;

import com.booking.api.dto.gdpr.AccountDeletionRequest;
import com.booking.api.dto.gdpr.AccountDeletionResponse;
import com.booking.api.dto.gdpr.DataExportResponse;
import com.booking.api.dto.response.BusinessGdprInfoResponse;
import com.booking.api.model.Business;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.service.GdprService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Business GDPR endpoints
 * Specific endpoints for business dashboard
 * Authorization handled by SecurityConfig
 */
@RestController
@RequestMapping("/api/businesses/gdpr")
@RequiredArgsConstructor
@Slf4j
public class BusinessGdprController {

    private final GdprService gdprService;
    private final BusinessRepository businessRepository;

    /**
     * Get GDPR information for business
     * GET /api/businesses/gdpr/info
     */
    @GetMapping("/info")
    public ResponseEntity<BusinessGdprInfoResponse> getGdprInfo(Authentication authentication) {
        log.info("=== GDPR INFO ENDPOINT CALLED ===");
        log.info("Authentication object: {}", authentication);
        log.info("Authentication principal: {}", authentication != null ? authentication.getPrincipal() : "NULL");
        log.info("Authentication authorities: {}", authentication != null ? authentication.getAuthorities() : "NULL");

        String email = authentication.getName();
        log.info("Business GDPR info request from: {}", email);

        Business business = businessRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        // Count future appointments in next 30 days
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime in30Days = now.plusDays(30);
        int futureAppointments = gdprService.countFutureAppointments(business.getId(), now, in30Days);

        List<String> dataCategories = Arrays.asList(
                "Informations du business",
                "Services proposés",
                "Clients enregistrés",
                "Historique des rendez-vous",
                "Horaires et disponibilités",
                "Paramètres et préférences",
                "Périodes de vacances"
        );

        BusinessGdprInfoResponse.BusinessGdprInfoResponseBuilder builder = BusinessGdprInfoResponse.builder()
                .businessName(business.getBusinessName())
                .email(business.getEmail())
                .createdAt(business.getCreatedAt() != null ? business.getCreatedAt().toString() : null)
                .deletionGracePeriodDays(30)
                .futureAppointmentsCount(futureAppointments)
                .dataCategories(dataCategories);

        // If deletion is pending, add deletion info
        if (business.getDeletedAt() != null) {
            builder.deletedAt(business.getDeletedAt().toString());
            builder.effectiveDeletionDate(business.getDeletedAt().plusDays(30).toString());
        }

        return ResponseEntity.ok(builder.build());
    }

    /**
     * Export business data (GDPR Article 20 - Right to Data Portability)
     * GET /api/businesses/gdpr/export
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportBusinessData(Authentication authentication) throws Exception {
        String email = authentication.getName();
        log.info("Business GDPR data export request from: {}", email);

        DataExportResponse exportData = gdprService.exportUserData(email, "BUSINESS");

        // Configure ObjectMapper with JavaTimeModule for LocalDateTime serialization
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Convert to JSON
        byte[] jsonData = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(exportData);

        // Set headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment",
                "export-donnees-business-" + System.currentTimeMillis() + ".json");
        headers.setContentLength(jsonData.length);

        return new ResponseEntity<>(jsonData, headers, HttpStatus.OK);
    }

    /**
     * Request business account deletion (GDPR Article 17 - Right to be Forgotten)
     * POST /api/businesses/gdpr/delete
     */
    @PostMapping("/delete")
    public ResponseEntity<AccountDeletionResponse> deleteBusinessAccount(
            @Valid @RequestBody AccountDeletionRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        log.info("Business GDPR account deletion request from: {}", email);

        if (request.getConfirmDeletion() == null || !request.getConfirmDeletion()) {
            throw new IllegalArgumentException("La confirmation est requise pour supprimer le compte");
        }

        AccountDeletionResponse response = gdprService.deleteUserAccount(email, "BUSINESS", request);

        // Get business ID for appointment cancellation
        Business business = businessRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));
        UUID businessId = business.getId();
        int appointmentsCount = response.getFutureAppointmentsCount() != null ? response.getFutureAppointmentsCount() : 0;

        // Cancel appointments and send emails AFTER the transaction completes
        // This runs in a separate thread to avoid transaction conflicts
        new Thread(() -> {
            try {
                Thread.sleep(200); // Wait for transaction to fully commit

                // Cancel all future appointments (sends emails to customers automatically)
                gdprService.cancelBusinessAppointments(businessId);

                // Send deletion confirmation email to business
                gdprService.sendBusinessDeletionEmail(email, appointmentsCount);
            } catch (Exception e) {
                log.error("Failed to process business deletion notifications: {}", e.getMessage(), e);
            }
        }).start();

        // Update message for business context
        response = AccountDeletionResponse.builder()
                .message("Votre compte business sera supprimé dans 30 jours. Tous les rendez-vous futurs ont été annulés. Vous pouvez annuler cette demande en nous contactant.")
                .deletionDate(response.getDeletionDate())
                .effectiveDate(response.getEffectiveDate())
                .canRecover(response.getCanRecover())
                .futureAppointmentsCount(response.getFutureAppointmentsCount())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel deletion request
     * POST /api/businesses/gdpr/cancel-deletion
     */
    @PostMapping("/cancel-deletion")
    public ResponseEntity<?> cancelDeletion(Authentication authentication) {
        String email = authentication.getName();
        log.info("Business GDPR cancel deletion request from: {}", email);

        gdprService.cancelDeletion(email);

        return ResponseEntity.ok().body(java.util.Map.of(
                "message", "Votre demande de suppression a été annulée avec succès. Votre compte restera actif."
        ));
    }
}
