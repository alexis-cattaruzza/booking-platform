package com.booking.api.service;

import com.booking.api.exception.NotFoundException;
import com.booking.api.model.AuditLog;
import com.booking.api.model.Business;
import com.booking.api.model.User;
import com.booking.api.repository.AuditLogRepository;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SECURITY: Audit service for logging security-sensitive operations
 * All audit operations are performed asynchronously to avoid impacting performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    /**
     * SECURITY: Log an audit event asynchronously
     * Uses REQUIRES_NEW transaction to ensure audit logs are persisted even if parent transaction fails
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit(
            User user,
            String action,
            AuditLog.AuditStatus status,
            HttpServletRequest request
    ) {
        logAudit(user, action, status, request, null, null, null, null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit(
            User user,
            String action,
            AuditLog.AuditStatus status,
            HttpServletRequest request,
            String errorMessage
    ) {
        logAudit(user, action, status, request, null, null, errorMessage, null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit(
            User user,
            String action,
            AuditLog.AuditStatus status,
            HttpServletRequest request,
            String resourceType,
            String resourceId,
            String errorMessage,
            Map<String, Object> details
    ) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .username(user != null ? user.getEmail() : "anonymous")
                    .action(action)
                    .status(status)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .errorMessage(errorMessage)
                    // .details(details) // Temporarily disabled - requires hypersistence-utils dependency
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: action={}, status={}, user={}", action, status,
                    user != null ? user.getEmail() : "anonymous");
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log audit event with username (for cases where User entity is not available)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuditWithUsername(
            String username,
            String action,
            AuditLog.AuditStatus status,
            HttpServletRequest request,
            String errorMessage
    ) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(null)
                    .username(username)
                    .action(action)
                    .status(status)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .errorMessage(errorMessage)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: action={}, status={}, username={}", action, status, username);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * SECURITY: Extract client IP address, considering proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    // Query methods for retrieving audit logs

    @Transactional(readOnly = true)
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByStatus(AuditLog.AuditStatus status, Pageable pageable) {
        return auditLogRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return auditLogRepository.findByDateRange(startDate, endDate, pageable);
    }

    /**
     * SECURITY: Helper method to create details map
     */
    public Map<String, Object> createDetails(String key, Object value) {
        Map<String, Object> details = new HashMap<>();
        details.put(key, value);
        return details;
    }

    public Map<String, Object> createDetails(Map<String, Object> values) {
        return new HashMap<>(values);
    }

    /**
     * ADMIN: Permanently delete a business and its associated user
     * Creates audit log before deletion
     */
    @Transactional
    public void hardDeleteBusiness(UUID businessId, String reason) {
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Admin {} is hard deleting business: {} with reason: {}", adminEmail, businessId, reason);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found with ID: " + businessId));

        String businessName = business.getBusinessName();
        UUID userId = business.getUser().getId();
        String userEmail = business.getUser().getEmail();

        // Create audit log before deletion
        AuditLog auditLog = AuditLog.builder()
                .user(null)
                .username(adminEmail)
                .action("HARD_DELETE_BUSINESS")
                .resourceType("Business")
                .resourceId(businessId.toString())
                .status(AuditLog.AuditStatus.SUCCESS)
                .errorMessage(String.format("Admin hard delete. Business: %s, User: %s, Reason: %s",
                            businessName, userEmail, reason))
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);

        // Delete business (cascade will delete services, appointments, schedules, etc.)
        businessRepository.delete(business);
        log.info("Business deleted: {} ({})", businessName, businessId);

        // Delete associated user
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userRepository.delete(user);
            log.info("User deleted: {} ({})", userEmail, userId);
        }

        log.info("Hard delete completed by admin: {}", adminEmail);
    }
}
