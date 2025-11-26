package com.booking.api.controller;

import com.booking.api.dto.request.AppointmentRequest;
import com.booking.api.dto.response.AppointmentResponse;
import com.booking.api.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final AppointmentService appointmentService;

    /**
     * Public endpoint to create a new appointment
     */
    @PostMapping("/{businessSlug}")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @PathVariable String businessSlug,
            @Valid @RequestBody AppointmentRequest request) {

        AppointmentResponse response = appointmentService.createAppointment(
                businessSlug, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Public endpoint to get appointment details by cancellation token
     */
    @GetMapping("/appointment/{cancellationToken}")
    public ResponseEntity<AppointmentResponse> getAppointment(
            @PathVariable String cancellationToken) {

        AppointmentResponse response = appointmentService.getAppointmentByToken(
                cancellationToken);

        return ResponseEntity.ok(response);
    }

    /**
     * Public endpoint to cancel an appointment
     */
    @PostMapping("/cancel/{cancellationToken}")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable String cancellationToken) {

        appointmentService.cancelAppointment(cancellationToken);
        return ResponseEntity.ok().build();
    }
}
