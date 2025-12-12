package com.booking.api.controller;

import com.booking.api.dto.request.AppointmentRequest;
import com.booking.api.dto.request.CancelAppointmentRequest;
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

    @PostMapping("/{businessSlug}")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @PathVariable String businessSlug,
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(businessSlug, request));
    }

    @GetMapping("/appointment/{cancellationToken}")
    public ResponseEntity<AppointmentResponse> getAppointment(
            @PathVariable String cancellationToken) {
        return ResponseEntity.ok(appointmentService.getAppointmentByToken(cancellationToken));
    }

    @PostMapping("/cancel/{cancellationToken}")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable String cancellationToken,
            @Valid @RequestBody CancelAppointmentRequest request) {
        appointmentService.cancelAppointment(cancellationToken, request.getCancellationReason());
        return ResponseEntity.ok().build();
    }
}
