package com.booking.api.service;

import com.booking.api.dto.request.ScheduleRequest;
import com.booking.api.dto.response.ScheduleResponse;
import com.booking.api.model.Business;
import com.booking.api.model.Schedule;
import com.booking.api.model.User;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.ScheduleRepository;
import com.booking.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    /**
     * Récupère tous les horaires du business de l'utilisateur connecté
     */
    public List<ScheduleResponse> getMySchedules() {
        Business business = getAuthenticatedUserBusiness();
        log.info("Getting schedules for business: {}", business.getId());

        List<Schedule> schedules = scheduleRepository.findByBusinessId(business.getId());
        return schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les horaires actifs d'un business public
     */
    public List<ScheduleResponse> getPublicSchedules(String businessSlug) {
        log.info("Getting public schedules for business slug: {}", businessSlug);

        Business business = businessRepository.findBySlug(businessSlug)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        List<Schedule> schedules = scheduleRepository.findByBusinessIdAndIsActiveTrue(business.getId());
        return schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crée ou met à jour un horaire pour un jour spécifique
     */
    @Transactional
    public ScheduleResponse createOrUpdateSchedule(ScheduleRequest request) {
        Business business = getAuthenticatedUserBusiness();
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(request.getDayOfWeek());

        log.info("Creating/updating schedule for business: {} on {}", business.getId(), dayOfWeek);

        // Validation: end time doit être après start time
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        // Chercher si un horaire existe déjà pour ce jour
        Schedule schedule = scheduleRepository.findByBusinessIdAndDayOfWeek(business.getId(), dayOfWeek)
                .orElse(Schedule.builder()
                        .business(business)
                        .dayOfWeek(dayOfWeek)
                        .build());

        // Mise à jour des champs
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setSlotDurationMinutes(request.getSlotDurationMinutes() != null ? request.getSlotDurationMinutes() : 30);
        schedule.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        schedule = scheduleRepository.save(schedule);
        log.info("Schedule saved successfully for {}", dayOfWeek);

        return mapToResponse(schedule);
    }

    /**
     * Met à jour un horaire existant
     */
    @Transactional
    public ScheduleResponse updateSchedule(UUID scheduleId, ScheduleRequest request) {
        Business business = getAuthenticatedUserBusiness();

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("Schedule does not belong to your business");
        }

        log.info("Updating schedule: {}", scheduleId);

        // Validation
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (!request.getEndTime().isAfter(request.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }
        }

        // Mise à jour
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        if (request.getSlotDurationMinutes() != null) {
            schedule.setSlotDurationMinutes(request.getSlotDurationMinutes());
        }
        if (request.getIsActive() != null) {
            schedule.setIsActive(request.getIsActive());
        }

        schedule = scheduleRepository.save(schedule);
        log.info("Schedule updated successfully: {}", scheduleId);

        return mapToResponse(schedule);
    }

    /**
     * Supprime un horaire (le désactive)
     */
    @Transactional
    public void deleteSchedule(UUID scheduleId) {
        Business business = getAuthenticatedUserBusiness();

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("Schedule does not belong to your business");
        }

        log.info("Deleting schedule: {}", scheduleId);

        // Soft delete
        schedule.setIsActive(false);
        scheduleRepository.save(schedule);

        log.info("Schedule deleted (deactivated) successfully: {}", scheduleId);
    }

    private Business getAuthenticatedUserBusiness() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return businessRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Business not found for user"));
    }

    private ScheduleResponse mapToResponse(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek().name())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .slotDurationMinutes(schedule.getSlotDurationMinutes())
                .isActive(schedule.getIsActive())
                .build();
    }
}
