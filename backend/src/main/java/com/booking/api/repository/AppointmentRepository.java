package com.booking.api.repository;

import com.booking.api.model.Appointment;
import com.booking.api.model.Appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByCancellationToken(String cancellationToken);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.business b " +
           "JOIN FETCH a.service s " +
           "JOIN FETCH a.customer c " +
           "WHERE a.cancellationToken = :cancellationToken")
    Optional<Appointment> findByCancellationTokenWithRelations(@Param("cancellationToken") String cancellationToken);

    @Query("SELECT a FROM Appointment a WHERE a.business.id = :businessId " +
           "AND a.appointmentDatetime >= :start " +
           "AND a.appointmentDatetime <= :end")
    List<Appointment> findByBusinessIdAndDateRange(
        @Param("businessId") UUID businessId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    List<Appointment> findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
        UUID businessId,
        LocalDateTime start,
        LocalDateTime end
    );

    List<Appointment> findByBusinessIdAndStatusInAndAppointmentDatetimeBetween(
        UUID businessId,
        List<AppointmentStatus> statuses,
        LocalDateTime start,
        LocalDateTime end
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.business.id = :businessId " +
           "AND a.status IN ('PENDING', 'CONFIRMED') " +
           "AND a.appointmentDatetime >= :start " +
           "AND a.appointmentDatetime < :end")
    List<Appointment> findActiveAppointmentsForLocking(
        @Param("businessId") UUID businessId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    List<Appointment> findByCustomerId(UUID customerId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.business.id = :businessId " +
           "AND a.status IN ('PENDING', 'CONFIRMED') " +
           "AND a.appointmentDatetime BETWEEN :start AND :end")
    long countUpcomingAppointments(
        @Param("businessId") UUID businessId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    List<Appointment> findByBusinessIdOrderByAppointmentDatetimeDesc(UUID businessId);

    List<Appointment> findByCustomerIdOrderByAppointmentDatetimeDesc(UUID customerId);

    List<Appointment> findByBusinessIdAndAppointmentDatetimeAfter(UUID businessId, LocalDateTime dateTime);

    List<Appointment> findByCustomerIdAndAppointmentDatetimeAfter(UUID customerId, LocalDateTime dateTime);

    List<Appointment> findByStatusAndAppointmentDatetimeBefore(AppointmentStatus status, LocalDateTime dateTime);

    List<Appointment> findByBusinessIdAndAppointmentDatetimeBetweenAndStatusNot(
        UUID businessId,
        LocalDateTime start,
        LocalDateTime end,
        AppointmentStatus status
    );
}
