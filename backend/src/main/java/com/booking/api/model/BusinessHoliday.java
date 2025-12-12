package com.booking.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "business_holidays", indexes = {
    @Index(name = "idx_business_holidays_business_id", columnList = "business_id"),
    @Index(name = "idx_business_holidays_dates", columnList = "start_date, end_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 500)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Check if a given date falls within this holiday period
     */
    public boolean isDateInHoliday(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Check if this holiday overlaps with another date range
     */
    public boolean overlaps(LocalDate otherStart, LocalDate otherEnd) {
        return !endDate.isBefore(otherStart) && !startDate.isAfter(otherEnd);
    }
}
