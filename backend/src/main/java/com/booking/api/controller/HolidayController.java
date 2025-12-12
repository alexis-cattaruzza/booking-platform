package com.booking.api.controller;

import com.booking.api.dto.request.BusinessHolidayRequest;
import com.booking.api.dto.response.BusinessHolidayResponse;
import com.booking.api.service.BusinessHolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class HolidayController {

    private final BusinessHolidayService holidayService;

    /**
     * GET /api/holidays/me
     * Get all holidays for authenticated business
     */
    @GetMapping("/holidays/me")
    public ResponseEntity<List<BusinessHolidayResponse>> getMyHolidays() {
        log.info("GET /api/holidays/me");
        return ResponseEntity.ok(holidayService.getMyHolidays());
    }

    /**
     * GET /api/holidays/me/upcoming
     * Get upcoming holidays for authenticated business
     */
    @GetMapping("/holidays/me/upcoming")
    public ResponseEntity<List<BusinessHolidayResponse>> getUpcomingHolidays() {
        log.info("GET /api/holidays/me/upcoming");
        return ResponseEntity.ok(holidayService.getUpcomingHolidays());
    }

    /**
     * GET /api/holidays/preview
     * Preview appointments that will be affected by a holiday period
     */
    @GetMapping("/holidays/preview")
    public ResponseEntity<List<UUID>> previewAffectedAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("GET /api/holidays/preview - startDate: {}, endDate: {}", startDate, endDate);
        return ResponseEntity.ok(holidayService.getAffectedAppointments(startDate, endDate));
    }

    /**
     * POST /api/holidays
     * Create a new holiday period
     */
    @PostMapping("/holidays")
    public ResponseEntity<BusinessHolidayResponse> createHoliday(
            @Valid @RequestBody BusinessHolidayRequest request
    ) {
        log.info("POST /api/holidays - startDate: {}, endDate: {}",
                request.getStartDate(), request.getEndDate());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(holidayService.createHoliday(request));
    }

    /**
     * PUT /api/holidays/{id}
     * Update a holiday period
     */
    @PutMapping("/holidays/{id}")
    public ResponseEntity<BusinessHolidayResponse> updateHoliday(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessHolidayRequest request
    ) {
        log.info("PUT /api/holidays/{}", id);
        return ResponseEntity.ok(holidayService.updateHoliday(id, request));
    }

    /**
     * DELETE /api/holidays/{id}
     * Delete a holiday period
     */
    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable UUID id) {
        log.info("DELETE /api/holidays/{}", id);
        holidayService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/businesses/{slug}/holidays
     * Get upcoming holidays for a public business (for customer booking page)
     */
    @GetMapping("/businesses/{slug}/holidays")
    public ResponseEntity<List<BusinessHolidayResponse>> getBusinessHolidays(
            @PathVariable String slug
    ) {
        log.info("GET /api/businesses/{}/holidays", slug);
        return ResponseEntity.ok(holidayService.getHolidaysBySlug(slug));
    }
}
