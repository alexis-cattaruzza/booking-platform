package com.booking.api.scheduler;

import com.booking.api.model.AuditLog;
import com.booking.api.model.Business;
import com.booking.api.model.User;
import com.booking.api.repository.AuditLogRepository;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Scheduler pour la suppression automatique des businesses marqués pour suppression
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessDeletionScheduler {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Supprime automatiquement les businesses marqués pour suppression depuis plus de 30 jours
     * S'exécute tous les jours à 3h00 du matin
     */
    @Scheduled(cron = "0 0 3 * * *") // Tous les jours à 3h00
    @Transactional
    public void autoDeleteBusinesses() {
        log.info("Starting automatic deletion of businesses marked for deletion over 30 days ago");

        LocalDateTime deletionThreshold = LocalDateTime.now().minusDays(30);
        List<Business> businessesToDelete = businessRepository.findByDeletedAtBefore(deletionThreshold);

        int deletedCount = 0;
        for (Business business : businessesToDelete) {
            try {
                UUID businessId = business.getId();
                String businessName = business.getBusinessName();
                UUID userId = business.getUser().getId();
                String userEmail = business.getUser().getEmail();

                log.info("Auto-deleting business: {} (ID: {}) and associated user: {} (ID: {})",
                         businessName, businessId, userEmail, userId);

                // Create audit log before deletion
                AuditLog auditLog = AuditLog.builder()
                    .user(null) // System action
                    .username("SYSTEM")
                    .action("DELETE_BUSINESS_AUTO")
                    .resourceType("Business")
                    .resourceId(businessId.toString())
                    .status(AuditLog.AuditStatus.SUCCESS)
                    .errorMessage(String.format("Automatic deletion after 30-day grace period. Business: %s, User: %s",
                                businessName, userEmail))
                    .build();
                auditLogRepository.save(auditLog);

                // Delete business (cascade will delete services, appointments, etc.)
                businessRepository.delete(business);

                // Delete associated user
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    userRepository.delete(user);
                    log.info("Deleted user: {} (ID: {})", userEmail, userId);
                }

                deletedCount++;
            } catch (Exception e) {
                log.error("Error deleting business ID: {} - {}", business.getId(), e.getMessage(), e);
            }
        }

        log.info("Automatic deletion finished: {} businesses and their users permanently deleted", deletedCount);
    }
}
