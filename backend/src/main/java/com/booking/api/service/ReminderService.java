package com.booking.api.service;

import com.booking.api.model.Appointment;
import com.booking.api.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    /**
     * Send reminder emails for appointments happening in 24 hours
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void sendAppointmentReminders() {
        log.info("Running appointment reminder job");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderStart = now.plusHours(23);  // 23 hours from now
        LocalDateTime reminderEnd = now.plusHours(25);    // 25 hours from now

        // Find appointments that need reminders
        List<Appointment> appointments = appointmentRepository
                .findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                        null, // Get all businesses
                        reminderStart,
                        reminderEnd
                );

        int remindersSent = 0;
        for (Appointment appointment : appointments) {
            // Only send reminders for pending or confirmed appointments
            if (appointment.getStatus() == Appointment.AppointmentStatus.PENDING ||
                appointment.getStatus() == Appointment.AppointmentStatus.CONFIRMED) {

                try {
                    emailService.sendAppointmentReminder(appointment);
                    remindersSent++;
                } catch (Exception e) {
                    log.error("Failed to send reminder for appointment {}", appointment.getId(), e);
                }
            }
        }

        log.info("Sent {} appointment reminders", remindersSent);
    }
}
