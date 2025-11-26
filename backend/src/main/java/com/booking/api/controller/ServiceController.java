package com.booking.api.controller;

import com.booking.api.dto.request.ServiceRequest;
import com.booking.api.dto.response.ServiceResponse;
import com.booking.api.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final ServiceService serviceService;

    /**
     * GET /api/services
     * Liste tous les services du business de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getMyServices() {
        log.info("GET /api/services");
        List<ServiceResponse> services = serviceService.getMyServices();
        return ResponseEntity.ok(services);
    }

    /**
     * GET /api/services/{id}
     * Récupère un service par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable UUID id) {
        log.info("GET /api/services/{}", id);
        ServiceResponse service = serviceService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    /**
     * POST /api/services
     * Crée un nouveau service
     */
    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        log.info("POST /api/services");
        ServiceResponse service = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(service);
    }

    /**
     * PUT /api/services/{id}
     * Met à jour un service existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable UUID id,
            @Valid @RequestBody ServiceRequest request
    ) {
        log.info("PUT /api/services/{}", id);
        ServiceResponse service = serviceService.updateService(id, request);
        return ResponseEntity.ok(service);
    }

    /**
     * DELETE /api/services/{id}
     * Supprime un service (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
        log.info("DELETE /api/services/{}", id);
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
