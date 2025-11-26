package com.booking.api.controller;

import com.booking.api.dto.request.ScheduleRequest;
import com.booking.api.dto.response.ScheduleResponse;
import com.booking.api.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * GET /api/schedules
     * Liste tous les horaires du business de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getMySchedules() {
        log.info("GET /api/schedules");
        List<ScheduleResponse> schedules = scheduleService.getMySchedules();
        return ResponseEntity.ok(schedules);
    }

    /**
     * POST /api/schedules
     * Crée ou met à jour un horaire pour un jour spécifique
     */
    @PostMapping
    public ResponseEntity<ScheduleResponse> createOrUpdateSchedule(@Valid @RequestBody ScheduleRequest request) {
        log.info("POST /api/schedules");
        ScheduleResponse schedule = scheduleService.createOrUpdateSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }

    /**
     * PUT /api/schedules/{id}
     * Met à jour un horaire existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable UUID id,
            @Valid @RequestBody ScheduleRequest request
    ) {
        log.info("PUT /api/schedules/{}", id);
        ScheduleResponse schedule = scheduleService.updateSchedule(id, request);
        return ResponseEntity.ok(schedule);
    }

    /**
     * DELETE /api/schedules/{id}
     * Supprime un horaire (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable UUID id) {
        log.info("DELETE /api/schedules/{}", id);
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
