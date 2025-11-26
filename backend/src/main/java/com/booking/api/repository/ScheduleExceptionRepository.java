package com.booking.api.repository;

import com.booking.api.model.ScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, UUID> {

    List<ScheduleException> findByBusinessIdAndExceptionDateBetween(
        UUID businessId,
        LocalDate startDate,
        LocalDate endDate
    );

    Optional<ScheduleException> findByBusinessIdAndExceptionDate(
        UUID businessId,
        LocalDate exceptionDate
    );

    boolean existsByBusinessIdAndExceptionDate(UUID businessId, LocalDate exceptionDate);
}
