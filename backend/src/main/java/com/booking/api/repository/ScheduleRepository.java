package com.booking.api.repository;

import com.booking.api.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    List<Schedule> findByBusinessIdAndIsActiveTrue(UUID businessId);

    Optional<Schedule> findByBusinessIdAndDayOfWeek(UUID businessId, DayOfWeek dayOfWeek);

    List<Schedule> findByBusinessId(UUID businessId);
}
