package com.booking.api.service;

import com.booking.api.model.Appointment;
import com.booking.api.model.Notification;
import com.booking.api.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    @Value("${app.mail.from:noreply@booking-platform.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Booking Platform}")
    private String fromName;

    @Value("${app.base-url:http://localhost:4200}")
    private String baseUrl;

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER =
        DateTimeFormatter.ofPattern("HH'h'mm");

    /**
     * Send booking confirmation email to customer
     */
    @Async
    public void sendBookingConfirmation(Appointment appointment) {
        try {
            String subject = "Confirmation de votre rendez-vous";
            String content = buildConfirmationEmail(appointment);

            sendEmail(appointment.getCustomer().getEmail(), subject, content);

            // Save notification
            saveNotification(appointment, Notification.NotificationType.CONFIRMATION,
                subject, content, Notification.NotificationStatus.SENT);

            log.info("Confirmation email sent for appointment {}", appointment.getId());

        } catch (Exception e) {
            log.error("Failed to send confirmation email for appointment {}",
                appointment.getId(), e);
            saveNotification(appointment, Notification.NotificationType.CONFIRMATION,
                "Confirmation email", "", Notification.NotificationStatus.FAILED);
        }
    }

    /**
     * Send appointment reminder email (24h before)
     */
    @Async
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            String subject = "Rappel : Votre rendez-vous demain";
            String content = buildReminderEmail(appointment);

            sendEmail(appointment.getCustomer().getEmail(), subject, content);

            saveNotification(appointment, Notification.NotificationType.REMINDER,
                subject, content, Notification.NotificationStatus.SENT);

            log.info("Reminder email sent for appointment {}", appointment.getId());

        } catch (Exception e) {
            log.error("Failed to send reminder email for appointment {}",
                appointment.getId(), e);
            saveNotification(appointment, Notification.NotificationType.REMINDER,
                "Reminder email", "", Notification.NotificationStatus.FAILED);
        }
    }

    /**
     * Send cancellation email to customer
     */
    @Async
    public void sendCancellationEmail(Appointment appointment) {
        try {
            String subject = "Annulation de votre rendez-vous";
            String content = buildCancellationEmail(appointment);

            sendEmail(appointment.getCustomer().getEmail(), subject, content);

            saveNotification(appointment, Notification.NotificationType.CANCELLATION,
                subject, content, Notification.NotificationStatus.SENT);

            log.info("Cancellation email sent for appointment {}", appointment.getId());

        } catch (Exception e) {
            log.error("Failed to send cancellation email for appointment {}",
                appointment.getId(), e);
            saveNotification(appointment, Notification.NotificationType.CANCELLATION,
                "Cancellation email", "", Notification.NotificationStatus.FAILED);
        }
    }

    /**
     * Send email with HTML content
     */
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Save notification record to database
     */
    private void saveNotification(Appointment appointment, Notification.NotificationType type,
                                   String subject, String content, Notification.NotificationStatus status) {
        Notification notification = Notification.builder()
                .appointment(appointment)
                .type(type)
                .channel(Notification.NotificationChannel.EMAIL)
                .recipient(appointment.getCustomer().getEmail())
                .subject(subject)
                .content(content)
                .status(status)
                .sentAt(status == Notification.NotificationStatus.SENT ? LocalDateTime.now() : null)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Build confirmation email HTML content
     */
    private String buildConfirmationEmail(Appointment appointment) {
        String businessName = appointment.getBusiness().getBusinessName();
        String serviceName = appointment.getService().getName();
        String dateStr = appointment.getAppointmentDatetime().format(DATE_FORMATTER);
        String timeStr = appointment.getAppointmentDatetime().format(TIME_FORMATTER);
        String duration = appointment.getDurationMinutes() + " minutes";
        String price = String.format("%.2f €", appointment.getPrice());
        String customerName = appointment.getCustomer().getFirstName();
        String cancelUrl = baseUrl + "/booking/cancel/" + appointment.getCancellationToken();

        return buildEmailTemplate(
            "Confirmation de votre rendez-vous",
            customerName,
            "<p>Votre rendez-vous a été confirmé avec succès !</p>" +
            "<div style='background-color: #f3f4f6; border-left: 4px solid #3b82f6; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 5px 0;'><strong>Service :</strong> " + serviceName + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Date :</strong> " + dateStr + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Heure :</strong> " + timeStr + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Durée :</strong> " + duration + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Prix :</strong> " + price + "</p>" +
            "</div>" +
            "<p><strong>Adresse :</strong><br>" +
            appointment.getBusiness().getAddress() + "<br>" +
            appointment.getBusiness().getPostalCode() + " " + appointment.getBusiness().getCity() + "</p>" +
            "<p>Si vous devez annuler votre rendez-vous, cliquez sur le lien ci-dessous :</p>" +
            "<p><a href='" + cancelUrl + "' style='color: #ef4444;'>Annuler mon rendez-vous</a></p>"
        );
    }

    /**
     * Build reminder email HTML content
     */
    private String buildReminderEmail(Appointment appointment) {
        String serviceName = appointment.getService().getName();
        String dateStr = appointment.getAppointmentDatetime().format(DATE_FORMATTER);
        String timeStr = appointment.getAppointmentDatetime().format(TIME_FORMATTER);
        String customerName = appointment.getCustomer().getFirstName();
        String cancelUrl = baseUrl + "/booking/cancel/" + appointment.getCancellationToken();

        return buildEmailTemplate(
            "Rappel : Votre rendez-vous demain",
            customerName,
            "<p>Nous vous rappelons que vous avez un rendez-vous demain :</p>" +
            "<div style='background-color: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 5px 0;'><strong>Service :</strong> " + serviceName + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Date :</strong> " + dateStr + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Heure :</strong> " + timeStr + "</p>" +
            "</div>" +
            "<p><strong>Adresse :</strong><br>" +
            appointment.getBusiness().getAddress() + "<br>" +
            appointment.getBusiness().getPostalCode() + " " + appointment.getBusiness().getCity() + "</p>" +
            "<p>Nous vous attendons avec plaisir !</p>" +
            "<p>En cas d'imprévu, vous pouvez annuler votre rendez-vous en cliquant sur le lien ci-dessous :</p>" +
            "<p><a href='" + cancelUrl + "' style='color: #ef4444;'>Annuler mon rendez-vous</a></p>"
        );
    }

    /**
     * Build cancellation email HTML content
     */
    private String buildCancellationEmail(Appointment appointment) {
        String serviceName = appointment.getService().getName();
        String dateStr = appointment.getAppointmentDatetime().format(DATE_FORMATTER);
        String timeStr = appointment.getAppointmentDatetime().format(TIME_FORMATTER);
        String customerName = appointment.getCustomer().getFirstName();
        String bookingUrl = baseUrl + "/booking/" + appointment.getBusiness().getSlug();

        return buildEmailTemplate(
            "Annulation de votre rendez-vous",
            customerName,
            "<p>Votre rendez-vous a été annulé :</p>" +
            "<div style='background-color: #fee2e2; border-left: 4px solid #ef4444; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 5px 0;'><strong>Service :</strong> " + serviceName + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Date :</strong> " + dateStr + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Heure :</strong> " + timeStr + "</p>" +
            "</div>" +
            "<p>Si vous souhaitez reprendre rendez-vous, cliquez sur le lien ci-dessous :</p>" +
            "<p><a href='" + bookingUrl + "' style='display: inline-block; padding: 10px 20px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 5px;'>Prendre un nouveau rendez-vous</a></p>"
        );
    }

    /**
     * Build email template with consistent design
     */
    private String buildEmailTemplate(String title, String customerName, String content) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "  <meta charset='UTF-8'>" +
            "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "  <title>" + title + "</title>" +
            "</head>" +
            "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
            "  <div style='background-color: #3b82f6; color: white; padding: 20px; text-align: center;'>" +
            "    <h1 style='margin: 0;'>" + fromName + "</h1>" +
            "  </div>" +
            "  <div style='background-color: white; padding: 30px; border: 1px solid #e5e7eb;'>" +
            "    <h2 style='color: #1f2937; margin-top: 0;'>" + title + "</h2>" +
            "    <p>Bonjour " + customerName + ",</p>" +
            content +
            "    <p style='margin-top: 30px;'>Cordialement,<br>L'équipe " + fromName + "</p>" +
            "  </div>" +
            "  <div style='background-color: #f9fafb; padding: 20px; text-align: center; font-size: 12px; color: #6b7280; border: 1px solid #e5e7eb; border-top: none;'>" +
            "    <p style='margin: 5px 0;'>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>" +
            "    <p style='margin: 5px 0;'>&copy; 2025 " + fromName + ". Tous droits réservés.</p>" +
            "  </div>" +
            "</body>" +
            "</html>";
    }
}
