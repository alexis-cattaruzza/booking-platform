package com.booking.api.controller;

import com.booking.api.dto.response.AvailabilityResponse;
import com.booking.api.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/{businessSlug}")
    public ResponseEntity<AvailabilityResponse> getAvailability(
            @PathVariable String businessSlug,
            @RequestParam UUID serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        AvailabilityResponse response = availabilityService.getAvailability(
                businessSlug, serviceId, date);

        return ResponseEntity.ok(response);
    }
}
