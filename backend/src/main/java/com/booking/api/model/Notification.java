package com.booking.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_appointment_id", columnList = "appointment_id"),
    @Index(name = "idx_notifications_status", columnList = "status"),
    @Index(name = "idx_notifications_type", columnList = "type"),
    @Index(name = "idx_notifications_sent_at", columnList = "sent_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false)
    private String recipient;

    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        CONFIRMATION,
        REMINDER,
        CANCELLATION,
        MODIFICATION
    }

    public enum NotificationChannel {
        EMAIL,
        SMS
    }

    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED,
        DELIVERED
    }
}
