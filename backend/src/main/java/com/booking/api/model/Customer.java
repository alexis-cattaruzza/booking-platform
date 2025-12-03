package com.booking.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_business_phone", columnNames = {"business_id", "phone"})
    },
    indexes = {
        @Index(name = "idx_customers_business_id", columnList = "business_id"),
        @Index(name = "idx_customers_phone", columnList = "phone"),
        @Index(name = "idx_customers_email", columnList = "email"),
        @Index(name = "idx_customers_name", columnList = "business_id, last_name, first_name")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "total_appointments")
    @Builder.Default
    private Integer totalAppointments = 0;

    @Column(name = "last_appointment_at")
    private LocalDateTime lastAppointmentAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
@Column(name = "deleted_at")    private LocalDateTime deletedAt;

    // Relations
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
}
