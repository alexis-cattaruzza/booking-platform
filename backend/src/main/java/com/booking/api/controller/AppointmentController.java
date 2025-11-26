package com.booking.api.controller;

import com.booking.api.dto.response.AppointmentResponse;
import com.booking.api.model.Appointment;
import com.booking.api.model.Business;
import com.booking.api.model.User;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.UserRepository;
import com.booking.api.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    /**
     * Get all appointments for the authenticated business within a date range
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        Business business = getAuthenticatedUserBusiness();
        List<AppointmentResponse> appointments = appointmentService.getBusinessAppointments(
                business.getId(), start, end);

        return ResponseEntity.ok(appointments);
    }

    /**
     * Update appointment status (confirm, complete, mark as no-show)
     */
    @PutMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable UUID appointmentId,
            @RequestParam Appointment.AppointmentStatus status) {

        AppointmentResponse response = appointmentService.updateAppointmentStatus(
                appointmentId, status);

        return ResponseEntity.ok(response);
    }

    private Business getAuthenticatedUserBusiness() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return businessRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Business not found for user"));
    }
}
