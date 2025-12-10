package com.booking.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * SECURITY: Audit log entity for tracking security-sensitive operations
 * Provides an immutable audit trail for compliance and security monitoring
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "username")
    private String username;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "resource_type", length = 100)
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuditStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Note: details field temporarily disabled - requires hypersistence-utils dependency for JSONB support
    // @Column(name = "details", columnDefinition = "jsonb")
    // private Map<String, Object> details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum AuditStatus {
        SUCCESS,
        FAILURE,
        ERROR
    }

    // Common audit action types
    public static final class Actions {
        public static final String LOGIN = "LOGIN";
        public static final String LOGIN_FAILED = "LOGIN_FAILED";
        public static final String LOGOUT = "LOGOUT";
        public static final String REGISTER = "REGISTER";
        public static final String EMAIL_VERIFIED = "EMAIL_VERIFIED";
        public static final String PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";
        public static final String PASSWORD_RESET_COMPLETED = "PASSWORD_RESET_COMPLETED";
        public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
        public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
        public static final String ACCOUNT_UNLOCKED = "ACCOUNT_UNLOCKED";
        public static final String PROFILE_UPDATED = "PROFILE_UPDATED";
        public static final String APPOINTMENT_CREATED = "APPOINTMENT_CREATED";
        public static final String APPOINTMENT_CANCELLED = "APPOINTMENT_CANCELLED";
        public static final String BUSINESS_PROFILE_CREATED = "BUSINESS_PROFILE_CREATED";
        public static final String BUSINESS_PROFILE_UPDATED = "BUSINESS_PROFILE_UPDATED";
        public static final String UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS";
    }
}
