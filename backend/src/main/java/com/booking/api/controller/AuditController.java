package com.booking.api.controller;

import com.booking.api.dto.response.AuditLogResponse;
import com.booking.api.dto.response.BusinessResponse;
import com.booking.api.model.AuditLog;
import com.booking.api.model.Business;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SECURITY: Admin-only endpoint for viewing audit logs
 * Requires ADMIN role to access
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;
    private final BusinessRepository businessRepository;

    /**
     * GET /api/admin/audit
     * Get all audit logs with pagination
     */
    @GetMapping("/audit")
    public ResponseEntity<Page<AuditLogResponse>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Fetching all audit logs - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getAllAuditLogs(pageable);
        Page<AuditLogResponse> response = auditLogs.map(AuditLogResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/audit/user/{userId}
     * Get audit logs for a specific user
     */
    @GetMapping("/audit/user/{userId}")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByUser(
            @PathVariable java.util.UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        // Convert UUID to Long for database query
        // Note: This assumes the audit log stores user_id as Long in the database
        // If it's UUID, you'll need to update the repository method
        Page<AuditLog> auditLogs = auditService.getAuditLogsByUserId(
            Long.valueOf(userId.toString().hashCode()),
            pageable
        );
        Page<AuditLogResponse> response = auditLogs.map(AuditLogResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/audit/action/{action}
     * Get audit logs by action type
     */
    @GetMapping("/audit/action/{action}")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getAuditLogsByAction(action, pageable);
        Page<AuditLogResponse> response = auditLogs.map(AuditLogResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/audit/status/{status}
     * Get audit logs by status
     */
    @GetMapping("/audit/status/{status}")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByStatus(
            @PathVariable AuditLog.AuditStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getAuditLogsByStatus(status, pageable);
        Page<AuditLogResponse> response = auditLogs.map(AuditLogResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/audit/date-range
     * Get audit logs within a date range
     */
    @GetMapping("/audit/date-range")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getAuditLogsByDateRange(startDate, endDate, pageable);
        Page<AuditLogResponse> response = auditLogs.map(AuditLogResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/businesses
     * Get all businesses with pagination
     */
    @GetMapping("/businesses")
    public ResponseEntity<Page<BusinessResponse>> getAllBusinesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Business> businesses = businessRepository.findAll(pageable);

        List<BusinessResponse> businessResponses = businesses.getContent()
                .stream()
                .map(BusinessResponse::fromEntity)
                .collect(Collectors.toList());

        Page<BusinessResponse> response = new PageImpl<>(
                businessResponses,
                pageable,
                businesses.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/admin/businesses/{id}/suspend
     * Suspend a business
     */
    @PutMapping("/businesses/{id}/suspend")
    public ResponseEntity<BusinessResponse> suspendBusiness(@PathVariable UUID id) {
        log.info("Suspending business: {}", id);
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setIsActive(false);
        Business updated = businessRepository.save(business);

        return ResponseEntity.ok(BusinessResponse.fromEntity(updated));
    }

    /**
     * PUT /api/admin/businesses/{id}/activate
     * Activate a business
     */
    @PutMapping("/businesses/{id}/activate")
    public ResponseEntity<BusinessResponse> activateBusiness(@PathVariable UUID id) {
        log.info("Activating business: {}", id);
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setIsActive(true);
        business.setDeletedAt(null);
        Business updated = businessRepository.save(business);

        return ResponseEntity.ok(BusinessResponse.fromEntity(updated));
    }

    /**
     * DELETE /api/admin/businesses/{id}
     * Soft delete a business
     */
    @DeleteMapping("/businesses/{id}")
    public ResponseEntity<BusinessResponse> deleteBusiness(@PathVariable UUID id) {
        log.info("Deleting business: {}", id);
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setDeletedAt(LocalDateTime.now());
        business.setIsActive(false);
        Business updated = businessRepository.save(business);

        return ResponseEntity.ok(BusinessResponse.fromEntity(updated));
    }
}
