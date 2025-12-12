package com.booking.api.service;

import com.booking.api.dto.request.BusinessHolidayRequest;
import com.booking.api.dto.response.BusinessHolidayResponse;
import com.booking.api.exception.BadRequestException;
import com.booking.api.exception.NotFoundException;
import com.booking.api.model.Appointment;
import com.booking.api.model.Business;
import com.booking.api.model.BusinessHoliday;
import com.booking.api.model.User;
import com.booking.api.repository.AppointmentRepository;
import com.booking.api.repository.BusinessHolidayRepository;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessHolidayService {

    private final BusinessHolidayRepository holidayRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Get all holidays for the authenticated business
     */
    public List<BusinessHolidayResponse> getMyHolidays() {
        Business business = getAuthenticatedBusiness();
        List<BusinessHoliday> holidays = holidayRepository.findByBusinessIdOrderByStartDateAsc(business.getId());
        return holidays.stream()
                .map(BusinessHolidayResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming holidays for the authenticated business
     */
    public List<BusinessHolidayResponse> getUpcomingHolidays() {
        Business business = getAuthenticatedBusiness();
        List<BusinessHoliday> holidays = holidayRepository.findUpcomingHolidaysByBusinessId(
                business.getId(),
                LocalDate.now()
        );
        return holidays.stream()
                .map(BusinessHolidayResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get holidays for a public business by slug
     */
    public List<BusinessHolidayResponse> getHolidaysBySlug(String slug) {
        Business business = businessRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Business not found"));

        List<BusinessHoliday> holidays = holidayRepository.findUpcomingHolidaysByBusinessId(
                business.getId(),
                LocalDate.now()
        );

        return holidays.stream()
                .map(BusinessHolidayResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create a new holiday period
     * Returns list of appointments that will be cancelled
     */
    @Transactional
    public BusinessHolidayResponse createHoliday(BusinessHolidayRequest request) {
        Business business = getAuthenticatedBusiness();

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after or equal to start date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot create holiday in the past");
        }

        // Check for overlapping holidays
        List<BusinessHoliday> overlapping = holidayRepository.findByBusinessIdAndDateRange(
                business.getId(),
                request.getStartDate(),
                request.getEndDate()
        );

        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Holiday period overlaps with existing holiday");
        }

        // Create holiday
        BusinessHoliday holiday = BusinessHoliday.builder()
                .business(business)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .build();

        holiday = holidayRepository.save(holiday);
        log.info("Holiday created for business {}: {} to {}", business.getId(),
                request.getStartDate(), request.getEndDate());

        // Cancel appointments during holiday period
        cancelAppointmentsInHolidayPeriod(business.getId(), request.getStartDate(), request.getEndDate());

        return BusinessHolidayResponse.fromEntity(holiday);
    }

    /**
     * Get appointments that will be affected by a holiday period (for preview before creation)
     */
    public List<UUID> getAffectedAppointments(LocalDate startDate, LocalDate endDate) {
        Business business = getAuthenticatedBusiness();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Appointment> appointments = appointmentRepository
                .findByBusinessIdAndAppointmentDatetimeBetweenAndStatusNot(
                        business.getId(),
                        startDateTime,
                        endDateTime,
                        Appointment.AppointmentStatus.CANCELLED
                );

        return appointments.stream()
                .map(Appointment::getId)
                .collect(Collectors.toList());
    }

    /**
     * Update a holiday period
     */
    @Transactional
    public BusinessHolidayResponse updateHoliday(UUID holidayId, BusinessHolidayRequest request) {
        Business business = getAuthenticatedBusiness();

        BusinessHoliday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new NotFoundException("Holiday not found"));

        if (!holiday.getBusiness().getId().equals(business.getId())) {
            throw new BadRequestException("Holiday does not belong to your business");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after or equal to start date");
        }

        // Check for overlapping holidays (excluding current holiday)
        List<BusinessHoliday> overlapping = holidayRepository.findByBusinessIdAndDateRange(
                business.getId(),
                request.getStartDate(),
                request.getEndDate()
        ).stream()
        .filter(h -> !h.getId().equals(holidayId))
        .collect(Collectors.toList());

        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Holiday period overlaps with existing holiday");
        }

        holiday.setStartDate(request.getStartDate());
        holiday.setEndDate(request.getEndDate());
        holiday.setReason(request.getReason());

        holiday = holidayRepository.save(holiday);
        log.info("Holiday updated: {}", holidayId);

        return BusinessHolidayResponse.fromEntity(holiday);
    }

    /**
     * Delete a holiday period
     */
    @Transactional
    public void deleteHoliday(UUID holidayId) {
        Business business = getAuthenticatedBusiness();

        BusinessHoliday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new NotFoundException("Holiday not found"));

        if (!holiday.getBusiness().getId().equals(business.getId())) {
            throw new BadRequestException("Holiday does not belong to your business");
        }

        holidayRepository.delete(holiday);
        log.info("Holiday deleted: {}", holidayId);
    }

    /**
     * Check if a date falls on a holiday for a business
     */
    public boolean isDateOnHoliday(UUID businessId, LocalDate date) {
        return holidayRepository.existsByBusinessIdAndDate(businessId, date);
    }

    private Business getAuthenticatedBusiness() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return businessRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Business not found"));
    }

    private void cancelAppointmentsInHolidayPeriod(UUID businessId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Appointment> appointments = appointmentRepository
                .findByBusinessIdAndAppointmentDatetimeBetweenAndStatusNot(
                        businessId,
                        startDateTime,
                        endDateTime,
                        Appointment.AppointmentStatus.CANCELLED
                );

        for (Appointment appointment : appointments) {
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            appointment.setCancelledBy(Appointment.CancelledBy.BUSINESS);
            appointmentRepository.save(appointment);
        }

        log.info("Cancelled {} appointments for holiday period", appointments.size());
    }
}
