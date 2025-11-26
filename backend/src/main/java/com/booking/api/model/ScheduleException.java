package com.booking.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_exceptions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_business_exception_date", columnNames = {"business_id", "exception_date"})
    },
    indexes = {
        @Index(name = "idx_exceptions_business_date", columnList = "business_id, exception_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleException {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "exception_date", nullable = false)
    private LocalDate exceptionDate;

    private String reason;

    @Column(name = "is_closed", nullable = false)
    @Builder.Default
    private Boolean isClosed = true;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
