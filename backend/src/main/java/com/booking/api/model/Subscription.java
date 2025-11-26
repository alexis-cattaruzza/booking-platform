package com.booking.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_business_subscription", columnNames = {"business_id"})
    },
    indexes = {
        @Index(name = "idx_subscriptions_business_id", columnList = "business_id"),
        @Index(name = "idx_subscriptions_plan", columnList = "plan"),
        @Index(name = "idx_subscriptions_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false, unique = true)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    @Builder.Default
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SubscriptionPlan {
        FREE,
        STARTER,
        PRO
    }

    public enum SubscriptionStatus {
        ACTIVE,
        CANCELLED,
        EXPIRED,
        SUSPENDED
    }
}
