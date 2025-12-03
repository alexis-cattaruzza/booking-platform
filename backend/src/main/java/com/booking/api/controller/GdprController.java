package com.booking.api.controller;

import com.booking.api.dto.gdpr.AccountDeletionRequest;
import com.booking.api.dto.gdpr.AccountDeletionResponse;
import com.booking.api.dto.gdpr.DataExportResponse;
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

/**
 * REST Controller for GDPR compliance endpoints
 * Article 20 - Right to Data Portability
 * Article 17 - Right to be Forgotten
 */
@RestController
@RequestMapping("/api/gdpr")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GdprController {

    private final GdprService gdprService;

    /**
     * Export user data (GDPR Article 20 - Right to Data Portability)
     * GET /api/gdpr/export?userType=BUSINESS|CUSTOMER
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUserData(
            @RequestParam String userType,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            log.info("GDPR data export request from: {} (type: {})", email, userType);

            DataExportResponse exportData = gdprService.exportUserData(email, userType);

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
                    "gdpr-data-export-" + System.currentTimeMillis() + ".json");
            headers.setContentLength(jsonData.length);

            return new ResponseEntity<>(jsonData, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error exporting GDPR data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Request account deletion (GDPR Article 17 - Right to be Forgotten)
     * POST /api/gdpr/delete?userType=BUSINESS|CUSTOMER
     */
    @PostMapping("/delete")
    public ResponseEntity<AccountDeletionResponse> deleteAccount(
            @RequestParam String userType,
            @Valid @RequestBody AccountDeletionRequest request,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            log.info("GDPR account deletion request from: {} (type: {})", email, userType);

            if (request.getConfirmDeletion() == null || !request.getConfirmDeletion()) {
                return ResponseEntity.badRequest().build();
            }

            AccountDeletionResponse response = gdprService.deleteUserAccount(email, userType, request);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid deletion request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        } catch (Exception e) {
            log.error("Error deleting account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get GDPR information and user rights
     * GET /api/gdpr/info
     */
    @GetMapping("/info")
    public ResponseEntity<GdprInfoResponse> getGdprInfo() {
        GdprInfoResponse info = new GdprInfoResponse(
                "GDPR Compliance Information",
                "Vous disposez des droits suivants conformément au RGPD :",
                new String[]{
                        "Droit d'accès (Article 15) - Obtenir une copie de vos données personnelles",
                        "Droit de rectification (Article 16) - Corriger vos données inexactes",
                        "Droit à l'effacement (Article 17) - Supprimer vos données ('droit à l'oubli')",
                        "Droit à la portabilité (Article 20) - Recevoir vos données dans un format structuré",
                        "Droit d'opposition (Article 21) - Vous opposer au traitement de vos données",
                        "Droit à la limitation (Article 18) - Limiter le traitement de vos données"
                },
                "Pour exercer vos droits, utilisez les endpoints /export et /delete ou contactez privacy@booking-platform.com",
                30
        );
        return ResponseEntity.ok(info);
    }

    /**
     * DTO for GDPR info response
     */
    record GdprInfoResponse(
            String title,
            String description,
            String[] rights,
            String contact,
            Integer deletionGracePeriodDays
    ) {
    }
}
