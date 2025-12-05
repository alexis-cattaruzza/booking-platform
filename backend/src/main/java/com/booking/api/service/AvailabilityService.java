package com.booking.api.service;

import com.booking.api.dto.response.AvailabilityResponse;
import com.booking.api.model.Appointment;
import com.booking.api.model.Business;
import com.booking.api.model.Schedule;
import com.booking.api.repository.AppointmentRepository;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.ScheduleExceptionRepository;
import com.booking.api.repository.ScheduleRepository;
import com.booking.api.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final BusinessRepository businessRepository;
    private final ServiceRepository serviceRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "availability", key = "#businessSlug + '_' + #serviceId + '_' + #date")
    public AvailabilityResponse getAvailability(String businessSlug, UUID serviceId, LocalDate date) {
        log.info("Getting availability for business: {}, service: {}, date: {}",
                businessSlug, serviceId, date);

        // Get business
        Business business = businessRepository.findBySlug(businessSlug)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // Get service
        com.booking.api.model.Service service = serviceRepository
                .findByIdAndBusinessId(serviceId, business.getId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (!service.getIsActive()) {
            throw new RuntimeException("Service is not active");
        }

        // Check if date is in the past
        if (date.isBefore(LocalDate.now())) {
            return AvailabilityResponse.builder()
                    .date(date)
                    .availableSlots(new ArrayList<>())
                    .build();
        }

        // Check for schedule exception (closed day)
        if (scheduleExceptionRepository.existsByBusinessIdAndExceptionDate(business.getId(), date)) {
            log.info("Business is closed on {} due to exception", date);
            return AvailabilityResponse.builder()
                    .date(date)
                    .availableSlots(new ArrayList<>())
                    .build();
        }

        // Get schedule for the day of week
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        log.info("Looking for schedule on {} for business {}", dayOfWeek, business.getId());

        Schedule schedule = scheduleRepository
                .findByBusinessIdAndDayOfWeek(business.getId(), dayOfWeek)
                .orElse(null);

        if (schedule == null || !schedule.getIsActive()) {
            log.warn("No active schedule found for {} on {} (schedule: {}, isActive: {})",
                    business.getBusinessName(), dayOfWeek,
                    schedule != null ? schedule.getId() : "null",
                    schedule != null ? schedule.getIsActive() : "N/A");
            return AvailabilityResponse.builder()
                    .date(date)
                    .availableSlots(new ArrayList<>())
                    .build();
        }

        log.info("Found schedule: {} - {} to {} (slot duration: {}min)",
                schedule.getId(), schedule.getStartTime(), schedule.getEndTime(),
                schedule.getSlotDurationMinutes());

        // Generate time slots
        List<AvailabilityResponse.TimeSlot> slots = generateTimeSlots(
                schedule, service, date, business.getId());

        long availableCount = slots.stream().filter(AvailabilityResponse.TimeSlot::isAvailable).count();
        log.info("Generated {} total slots, {} available for date {}",
                slots.size(), availableCount, date);

        return AvailabilityResponse.builder()
                .date(date)
                .availableSlots(slots)
                .build();
    }

    private List<AvailabilityResponse.TimeSlot> generateTimeSlots(
            Schedule schedule,
            com.booking.api.model.Service service,
            LocalDate date,
            UUID businessId) {

        List<AvailabilityResponse.TimeSlot> slots = new ArrayList<>();

        LocalTime currentTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        int serviceDuration = service.getDurationMinutes();
        int slotDuration = schedule.getSlotDurationMinutes() != null
                ? schedule.getSlotDurationMinutes()
                : 30;

        // Get existing appointments for this date
        LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

        List<Appointment> existingAppointments = appointmentRepository
                .findByBusinessIdAndDateRange(businessId, dayStart, dayEnd);

        // Generate slots
        while (currentTime.plusMinutes(serviceDuration).isBefore(endTime) ||
                currentTime.plusMinutes(serviceDuration).equals(endTime)) {

            LocalTime slotStart = currentTime;
            LocalTime slotEnd = currentTime.plusMinutes(serviceDuration);

            // Check if slot is in the past
            boolean isPast = false;
            if (date.equals(LocalDate.now())) {
                isPast = slotStart.isBefore(LocalTime.now());
            }

            // Check if slot conflicts with existing appointments
            boolean isAvailable = !isPast && isSlotAvailable(
                    slotStart, slotEnd, date, existingAppointments);

            slots.add(AvailabilityResponse.TimeSlot.builder()
                    .startTime(slotStart)
                    .endTime(slotEnd)
                    .available(isAvailable)
                    .build());

            currentTime = currentTime.plusMinutes(slotDuration);
        }

        return slots;
    }

    private boolean isSlotAvailable(
            LocalTime slotStart,
            LocalTime slotEnd,
            LocalDate date,
            List<Appointment> existingAppointments) {

        LocalDateTime requestedStart = LocalDateTime.of(date, slotStart);
        LocalDateTime requestedEnd = LocalDateTime.of(date, slotEnd);

        for (Appointment appointment : existingAppointments) {
            // Skip cancelled appointments
            if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
                continue;
            }

            LocalDateTime appointmentStart = appointment.getAppointmentDatetime();
            LocalDateTime appointmentEnd = appointmentStart.plusMinutes(
                    appointment.getDurationMinutes());

            // Check for overlap
            // Two time ranges overlap if: start1 < end2 AND start2 < end1
            if (requestedStart.isBefore(appointmentEnd) &&
                    appointmentStart.isBefore(requestedEnd)) {
                return false; // Slot is not available
            }
        }

        return true; // Slot is available
    }
}
