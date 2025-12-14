package com.booking.api.controller;

import com.booking.api.dto.request.ChangePasswordRequest;
import com.booking.api.dto.request.UpdateBusinessRequest;
import com.booking.api.dto.response.BusinessResponse;
import com.booking.api.dto.response.ServiceResponse;
import com.booking.api.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
@Slf4j
public class BusinessController {

    private final BusinessService businessService;

    /**
     * GET /api/businesses/me
     * Récupère le business de l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<BusinessResponse> getMyBusiness() {
        log.info("GET /api/businesses/me");
        return ResponseEntity.ok(businessService.getMyBusiness());
    }

    /**
     * PUT /api/businesses/me
     * Met à jour le business de l'utilisateur connecté
     */
    @PutMapping("/me")
    public ResponseEntity<BusinessResponse> updateMyBusiness(@Valid @RequestBody UpdateBusinessRequest request) {
        log.info("PUT /api/businesses/me");
        return ResponseEntity.ok(businessService.updateMyBusiness(request));
    }

    /**
     * GET /api/businesses/{slug}
     * Récupère un business public par son slug
     */
    @GetMapping("/{slug}")
    public ResponseEntity<BusinessResponse> getBusinessBySlug(@PathVariable String slug) {
        log.info("GET /api/businesses/{}", slug);
        return ResponseEntity.ok(businessService.getBusinessBySlug(slug));
    }

    /**
     * GET /api/businesses/{slug}/services
     * Récupère les services actifs d'un business public
     */
    @GetMapping("/{slug}/services")
    public ResponseEntity<List<ServiceResponse>> getBusinessServices(@PathVariable String slug) {
        log.info("GET /api/businesses/{}/services", slug);
        return ResponseEntity.ok(businessService.getBusinessServices(slug));
    }

    /**
     * POST /api/businesses/cancel-deletion
     * Annule la suppression programmée du business (si dans les 30 jours)
     */
    @PostMapping("/cancel-deletion")
    public ResponseEntity<BusinessResponse> cancelDeletion() {
        log.info("POST /api/businesses/cancel-deletion");
        return ResponseEntity.ok(businessService.cancelBusinessDeletion());
    }

    /**
     * POST /api/businesses/change-password
     * Change le mot de passe du business connecté
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("POST /api/businesses/change-password");
        businessService.changePassword(request);
        return ResponseEntity.ok().body(java.util.Map.of(
                "message", "Mot de passe modifié avec succès"
        ));
    }
}
