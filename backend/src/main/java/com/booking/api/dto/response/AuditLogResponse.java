package com.booking.api.dto.response;

import com.booking.api.model.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private UUID userId;
    private String username;
    private String action;
    private String resourceType;
    private String resourceId;
    private String ipAddress;
    private String userAgent;
    private AuditLog.AuditStatus status;
    private String errorMessage;
    // private Map<String, Object> details; // Temporarily disabled
    private LocalDateTime createdAt;

    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUser() != null ? auditLog.getUser().getId() : null)
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .resourceType(auditLog.getResourceType())
                .resourceId(auditLog.getResourceId())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .status(auditLog.getStatus())
                .errorMessage(auditLog.getErrorMessage())
                // .details(auditLog.getDetails()) // Temporarily disabled
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
