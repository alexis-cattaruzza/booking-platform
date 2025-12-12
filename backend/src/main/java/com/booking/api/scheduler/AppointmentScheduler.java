package com.booking.api.scheduler;

import com.booking.api.model.Appointment;
import com.booking.api.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler pour les tâches automatiques liées aux rendez-vous
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentScheduler {

    private final AppointmentRepository appointmentRepository;

    /**
     * Marque automatiquement les rendez-vous comme COMPLETED le lendemain à 9h
     * S'exécute tous les jours à 9h00
     */
    @Scheduled(cron = "0 0 9 * * *") // Tous les jours à 9h00
    @Transactional
    public void autoCompleteAppointments() {
        log.info("Starting auto-completion of past appointments");

        // Trouver tous les rendez-vous CONFIRMED dont la date est passée
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> pastAppointments = appointmentRepository
                .findByStatusAndAppointmentDatetimeBefore(
                        Appointment.AppointmentStatus.CONFIRMED,
                        now
                );

        int completedCount = 0;
        for (Appointment appointment : pastAppointments) {
            // Vérifier que le RDV est vraiment passé (plus de sécurité)
            if (appointment.getAppointmentDatetime().isBefore(now)) {
                appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
                appointmentRepository.save(appointment);
                completedCount++;
                log.debug("Auto-completed appointment ID: {}", appointment.getId());
            }
        }

        log.info("Auto-completion finished: {} appointments marked as COMPLETED", completedCount);
    }
}
