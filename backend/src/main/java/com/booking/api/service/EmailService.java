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

import java.io.UnsupportedEncodingException;
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
     * Send booking link email to business owner
     */
    @Async
    public void sendBookingLinkEmail(String businessEmail, String businessName, String slug) {
        try {
            String subject = "Votre lien de réservation est prêt !";
            String content = buildBookingLinkEmail(businessName, slug);

            sendEmail(businessEmail, subject, content);

            log.info("Booking link email sent to business {}", businessEmail);

        } catch (Exception e) {
            log.error("Failed to send booking link email to business {}", businessEmail, e);
        }
    }

    /**
     * Send email with HTML content
     * @throws UnsupportedEncodingException 
     */
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException, UnsupportedEncodingException {
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
        String serviceName = appointment.getService().getName();
        String dateStr = appointment.getAppointmentDatetime().format(DATE_FORMATTER);
        String timeStr = appointment.getAppointmentDatetime().format(TIME_FORMATTER);
        String duration = appointment.getDurationMinutes() + " minutes";
        String price = String.format("%.2f €", appointment.getPrice());
        String customerName = appointment.getCustomer().getFirstName();
        String cancelUrl = baseUrl + "/cancel/" + appointment.getCancellationToken();

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
            "<div style='background-color: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 0 0 10px 0; font-weight: bold; color: #92400e;'>⚠️ Politique d'annulation</p>" +
            "  <p style='margin: 0; color: #92400e; font-size: 14px;'>" +
            "    Vous pouvez annuler gratuitement votre rendez-vous <strong>jusqu'à 3 jours avant</strong> la date prévue. " +
            "    Passé ce délai, veuillez contacter directement l'établissement pour toute modification." +
            "  </p>" +
            "</div>" +
            "<p>Pour annuler votre rendez-vous, cliquez sur le lien ci-dessous :</p>" +
            "<p><a href='" + cancelUrl + "' style='color: #ef4444; text-decoration: underline;'>Annuler mon rendez-vous</a></p>"
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
        String cancelUrl = baseUrl + "/cancel/" + appointment.getCancellationToken();

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
        String businessName = appointment.getBusiness().getBusinessName();
        String bookingUrl = baseUrl + "/book/" + appointment.getBusiness().getSlug();

        return buildEmailTemplate(
            "Confirmation d'annulation de votre rendez-vous",
            customerName,
            "<div style='background-color: #dcfce7; border-left: 4px solid #16a34a; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 0; color: #166534;'><strong>✓ Annulation confirmée</strong></p>" +
            "  <p style='margin: 5px 0 0 0; color: #166534; font-size: 14px;'>Votre rendez-vous a été annulé avec succès.</p>" +
            "</div>" +
            "<p>Voici le récapitulatif du rendez-vous annulé :</p>" +
            "<div style='background-color: #fee2e2; border-left: 4px solid #ef4444; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 5px 0;'><strong>Établissement :</strong> " + businessName + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Service :</strong> " + serviceName + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Date :</strong> " + dateStr + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Heure :</strong> " + timeStr + "</p>" +
            "</div>" +
            "<p>Nous espérons vous revoir bientôt !</p>" +
            "<p>Si vous souhaitez reprendre rendez-vous, cliquez sur le bouton ci-dessous :</p>" +
            "<p style='text-align: center; margin: 30px 0;'>" +
            "  <a href='" + bookingUrl + "' style='display: inline-block; padding: 12px 24px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 6px; font-weight: 600;'>Prendre un nouveau rendez-vous</a>" +
            "</p>" +
            "<p style='color: #6b7280; font-size: 14px; margin-top: 30px;'>Si vous avez des questions, n'hésitez pas à contacter directement l'établissement.</p>"
        );
    }

    /**
     * Build booking link email HTML content for business owners
     */
    private String buildBookingLinkEmail(String businessName, String slug) {
        String bookingUrl = baseUrl + "/book/" + slug;

        return buildEmailTemplate(
            "Votre lien de réservation est prêt",
            businessName,
            "<p>Félicitations ! Votre profil business a été configuré avec succès.</p>" +
            "<p>Vos clients peuvent maintenant prendre rendez-vous en ligne en utilisant le lien ci-dessous :</p>" +
            "<div style='background-color: #dbeafe; border-left: 4px solid #3b82f6; padding: 20px; margin: 25px 0; text-align: center;'>" +
            "  <p style='margin: 0 0 15px 0; font-size: 14px; color: #1e40af;'><strong>Votre lien de réservation :</strong></p>" +
            "  <p style='margin: 0; font-family: monospace; font-size: 16px; word-break: break-all;'>" +
            "    <a href='" + bookingUrl + "' style='color: #2563eb; text-decoration: none;'>" + bookingUrl + "</a>" +
            "  </p>" +
            "</div>" +
            "<h3 style='color: #1f2937; margin-top: 30px;'>Comment utiliser ce lien ?</h3>" +
            "<ul style='color: #4b5563; line-height: 1.8;'>" +
            "  <li><strong>Partagez-le sur vos réseaux sociaux</strong> (Facebook, Instagram, LinkedIn, etc.)</li>" +
            "  <li><strong>Ajoutez-le à votre site web</strong> ou à votre signature email</li>" +
            "  <li><strong>Envoyez-le par SMS ou email</strong> à vos clients réguliers</li>" +
            "  <li><strong>Imprimez-le sur vos cartes de visite</strong> ou flyers</li>" +
            "</ul>" +
            "<div style='background-color: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 25px 0;'>" +
            "  <p style='margin: 0; color: #92400e;'><strong>💡 Conseil :</strong> Plus vous partagez ce lien, plus vos clients pourront facilement prendre rendez-vous avec vous !</p>" +
            "</div>" +
            "<h3 style='color: #1f2937; margin-top: 30px;'>Prochaines étapes</h3>" +
            "<ol style='color: #4b5563; line-height: 1.8;'>" +
            "  <li>Assurez-vous d'avoir ajouté vos <strong>services</strong> et leurs tarifs</li>" +
            "  <li>Configurez vos <strong>horaires d'ouverture</strong></li>" +
            "  <li>Testez votre lien de réservation en tant que client</li>" +
            "</ol>" +
            "<p style='margin-top: 30px;'>Connectez-vous à votre <a href='" + baseUrl + "/dashboard' style='color: #3b82f6;'>tableau de bord</a> pour gérer vos rendez-vous et paramètres.</p>"
        );
    }

    /**
     * SECURITY: Send email verification to new user
     */
    @Async
    public void sendVerificationEmail(String email, String firstName, String verificationToken) {
        try {
            String subject = "Vérifiez votre adresse email";
            String content = buildVerificationEmail(firstName, verificationToken);

            sendEmail(email, subject, content);

            log.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Build verification email content
     */
    private String buildVerificationEmail(String firstName, String verificationToken) {
        String verificationUrl = baseUrl + "/verify-email?token=" + verificationToken;

        String content = "    <p>Merci de vous être inscrit sur notre plateforme !</p>" +
            "    <p>Pour activer votre compte, veuillez vérifier votre adresse email en cliquant sur le bouton ci-dessous :</p>" +
            "    <div style='text-align: center; margin: 30px 0;'>" +
            "      <a href='" + verificationUrl + "' " +
            "         style='background-color: #3b82f6; color: white; padding: 12px 30px; text-decoration: none; " +
            "                border-radius: 5px; display: inline-block; font-weight: bold;'>" +
            "        Vérifier mon email" +
            "      </a>" +
            "    </div>" +
            "    <p>Ou copiez ce lien dans votre navigateur :</p>" +
            "    <p style='background-color: #f3f4f6; padding: 10px; word-break: break-all; font-size: 12px;'>" +
            verificationUrl +
            "    </p>" +
            "    <p style='color: #dc2626; font-weight: bold;'>⚠️ Ce lien est valable pendant 24 heures.</p>" +
            "    <p style='color: #6b7280; font-size: 14px;'>Si vous n'avez pas créé de compte, ignorez cet email.</p>";

        return buildEmailTemplate("Vérifiez votre adresse email", firstName, content);
    }

    /**
     * SECURITY: Send password reset email
     */
    @Async
    public void sendPasswordResetEmail(String email, String firstName, String resetToken) {
        try {
            String subject = "Réinitialisation de votre mot de passe";
            String content = buildPasswordResetEmail(firstName, resetToken);

            sendEmail(email, subject, content);

            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Build password reset email content
     */
    private String buildPasswordResetEmail(String firstName, String resetToken) {
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;

        String content = "    <p>Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte.</p>" +
            "    <p>Pour définir un nouveau mot de passe, cliquez sur le bouton ci-dessous :</p>" +
            "    <div style='text-align: center; margin: 30px 0;'>" +
            "      <a href='" + resetUrl + "' " +
            "         style='background-color: #3b82f6; color: white; padding: 12px 30px; text-decoration: none; " +
            "                border-radius: 5px; display: inline-block; font-weight: bold;'>" +
            "        Réinitialiser mon mot de passe" +
            "      </a>" +
            "    </div>" +
            "    <p>Ou copiez ce lien dans votre navigateur :</p>" +
            "    <p style='background-color: #f3f4f6; padding: 10px; word-break: break-all; font-size: 12px;'>" +
            resetUrl +
            "    </p>" +
            "    <p style='color: #dc2626; font-weight: bold;'>⚠️ Ce lien est valable pendant 1 heure.</p>" +
            "    <p style='color: #6b7280; font-size: 14px;'>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email. Votre mot de passe restera inchangé.</p>";

        return buildEmailTemplate("Réinitialisation de mot de passe", firstName, content);
    }

    /**
     * NEW: Send cancellation email to customer (initiated by customer)
     */
    @Async
    public void sendCustomerCancellationEmail(Appointment appointment) {
        try {
            String subject = "Annulation de votre rendez-vous";
            String content = buildCustomerCancellationEmail(appointment);

            sendEmail(appointment.getCustomer().getEmail(), subject, content);

            saveNotification(appointment, Notification.NotificationType.CANCELLATION,
                subject, content, Notification.NotificationStatus.SENT);

            log.info("Customer cancellation email sent for appointment {}", appointment.getId());

        } catch (Exception e) {
            log.error("Failed to send customer cancellation email for appointment {}",
                appointment.getId(), e);
            saveNotification(appointment, Notification.NotificationType.CANCELLATION,
                "Cancellation email", "", Notification.NotificationStatus.FAILED);
        }
    }

    /**
     * NEW: Send cancellation notification to business (customer cancelled)
     */
    @Async
    public void sendBusinessCancellationNotification(Appointment appointment) {
        try {
            String subject = "Annulation de rendez-vous - " + appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName();
            String content = buildBusinessCancellationNotification(appointment);

            sendEmail(appointment.getBusiness().getEmail(), subject, content);

            log.info("Business cancellation notification sent for appointment {}", appointment.getId());

        } catch (Exception e) {
            log.error("Failed to send business cancellation notification for appointment {}",
                appointment.getId(), e);
        }
    }

    /**
     * NEW: Send cancellation email to customer (initiated by business)
     */
    @Async
    public void sendBusinessInitiatedCancellationEmail(Appointment appointment) {
        try {
            String subject = "Annulation de votre rendez-vous par l'établissement";
            String content = buildBusinessInitiatedCancellationEmail(appointment);

            sendEmail(appointment.getCustomer().getEmail(), subject, content);

            saveNotification(appointment, Notification.NotificationType.CANCELLATION,
                subject, content, Notification.NotificationStatus.SENT);

            log.info("Business-initiated cancellation email sent for appointment {}", appointment.getId());

        } catch (Exception e) {
            log.error("Failed to send business-initiated cancellation email for appointment {}",
                appointment.getId(), e);
            saveNotification(appointment, Notification.NotificationType.CANCELLATION,
                "Cancellation email", "", Notification.NotificationStatus.FAILED);
        }
    }

    /**
     * Build customer cancellation email (when customer cancels)
     */
    private String buildCustomerCancellationEmail(Appointment appointment) {
        String serviceName = appointment.getService().getName();
        String dateStr = appointment.getAppointmentDatetime().format(DATE_FORMATTER);
        String timeStr = appointment.getAppointmentDatetime().format(TIME_FORMATTER);
        String customerName = appointment.getCustomer().getFirstName();
        String businessName = appointment.getBusiness().getBusinessName();
        String bookingUrl = baseUrl + "/book/" + appointment.getBusiness().getSlug();
        String reason = appointment.getCancellationReason() != null ? appointment.getCancellationReason() : "Non spécifiée";

        return buildEmailTemplate(
            "Confirmation d'annulation de votre rendez-vous",
            customerName,
            "<div style='background-color: #dcfce7; border-left: 4px solid #16a34a; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 0; color: #166534;'><strong>✓ Annulation confirmée</strong></p>" +
            "  <p style='margin: 5px 0 0 0; color: #166534; font-size: 14px;'>Votre rendez-vous a été annulé avec succès.</p>" +
            "</div>" +
            "<p>Voici le récapitulatif du rendez-vous annulé :</p>" +
            "<div style='background-color: #fee2e2; border-left: 4px solid #ef4444; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 5px 0;'><strong>Établissement :</strong> " + businessName + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Service :</strong> " + serviceName + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Date :</strong> " + dateStr + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Heure :</strong> " + timeStr + "</p>" +
            "</div>" +
            "<div style='background-color: #fffbeb; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 0 0 5px 0; font-weight: bold; color: #92400e;'>Raison de l'annulation :</p>" +
            "  <p style='margin: 0; color: #78350f;'>" + reason + "</p>" +
            "</div>" +
            "<p>L'établissement a été informé de votre annulation.</p>" +
            "<p>Nous espérons vous revoir bientôt !</p>" +
            "<p>Si vous souhaitez reprendre rendez-vous, cliquez sur le bouton ci-dessous :</p>" +
            "<p style='text-align: center; margin: 30px 0;'>" +
            "  <a href='" + bookingUrl + "' style='display: inline-block; padding: 12px 24px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 6px; font-weight: 600;'>Prendre un nouveau rendez-vous</a>" +
            "</p>"
        );
    }

    /**
     * Build business notification email (when customer cancels)
     */
    private String buildBusinessCancellationNotification(Appointment appointment) {
        String serviceName = appointment.getService().getName();
        String dateStr = appointment.getAppointmentDatetime().format(DATE_FORMATTER);
        String timeStr = appointment.getAppointmentDatetime().format(TIME_FORMATTER);
        String customerName = appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName();
        String customerEmail = appointment.getCustomer().getEmail();
        String customerPhone = appointment.getCustomer().getPhone();
        String reason = appointment.getCancellationReason() != null ? appointment.getCancellationReason() : "Non spécifiée";

        return buildEmailTemplate(
            "Annulation de rendez-vous",
            appointment.getBusiness().getBusinessName(),
            "<div style='background-color: #fee2e2; border-left: 4px solid #ef4444; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 0; color: #991b1b;'><strong>⚠️ Rendez-vous annulé par le client</strong></p>" +
            "</div>" +
            "<p>Un client a annulé son rendez-vous :</p>" +
            "<div style='background-color: #f3f4f6; border-left: 4px solid #6b7280; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 5px 0;'><strong>Client :</strong> " + customerName + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Email :</strong> " + customerEmail + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Téléphone :</strong> " + customerPhone + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Service :</strong> " + serviceName + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Date :</strong> " + dateStr + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Heure :</strong> " + timeStr + "</p>" +
            "</div>" +
            "<div style='background-color: #fffbeb; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 0 0 5px 0; font-weight: bold; color: #92400e;'>Raison de l'annulation :</p>" +
            "  <p style='margin: 0; color: #78350f;'>" + reason + "</p>" +
            "</div>" +
            "<p>Ce créneau est maintenant disponible pour d'autres réservations.</p>" +
            "<p style='text-align: center; margin: 30px 0;'>" +
            "  <a href='" + baseUrl + "/dashboard' style='display: inline-block; padding: 12px 24px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 6px; font-weight: 600;'>Voir mon agenda</a>" +
            "</p>"
        );
    }

    /**
     * Build customer email (when business cancels)
     */
    private String buildBusinessInitiatedCancellationEmail(Appointment appointment) {
        String serviceName = appointment.getService().getName();
        String dateStr = appointment.getAppointmentDatetime().format(DATE_FORMATTER);
        String timeStr = appointment.getAppointmentDatetime().format(TIME_FORMATTER);
        String customerName = appointment.getCustomer().getFirstName();
        String businessName = appointment.getBusiness().getBusinessName();
        String businessPhone = appointment.getBusiness().getPhone();
        String businessEmail = appointment.getBusiness().getEmail();
        String bookingUrl = baseUrl + "/book/" + appointment.getBusiness().getSlug();
        String reason = appointment.getCancellationReason() != null ? appointment.getCancellationReason() : "Non spécifiée";

        return buildEmailTemplate(
            "Annulation de votre rendez-vous",
            customerName,
            "<div style='background-color: #fee2e2; border-left: 4px solid #ef4444; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 0; color: #991b1b;'><strong>⚠️ Votre rendez-vous a été annulé</strong></p>" +
            "  <p style='margin: 5px 0 0 0; color: #991b1b; font-size: 14px;'>L'établissement a dû annuler votre rendez-vous.</p>" +
            "</div>" +
            "<p>Voici le récapitulatif du rendez-vous annulé :</p>" +
            "<div style='background-color: #f3f4f6; border-left: 4px solid #6b7280; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 5px 0;'><strong>Établissement :</strong> " + businessName + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Service :</strong> " + serviceName + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Date :</strong> " + dateStr + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Heure :</strong> " + timeStr + "</p>" +
            "</div>" +
            "<div style='background-color: #fffbeb; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 0 0 5px 0; font-weight: bold; color: #92400e;'>Raison de l'annulation :</p>" +
            "  <p style='margin: 0; color: #78350f;'>" + reason + "</p>" +
            "</div>" +
            "<p>Nous sommes désolés pour ce désagrément. N'hésitez pas à contacter directement l'établissement pour plus d'informations ou pour reprendre rendez-vous :</p>" +
            "<div style='background-color: #dbeafe; border-left: 4px solid #3b82f6; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 5px 0;'><strong>Téléphone :</strong> " + businessPhone + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Email :</strong> " + businessEmail + "</p>" +
            "</div>" +
            "<p style='text-align: center; margin: 30px 0;'>" +
            "  <a href='" + bookingUrl + "' style='display: inline-block; padding: 12px 24px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 6px; font-weight: 600;'>Prendre un nouveau rendez-vous</a>" +
            "</p>"
        );
    }

    /**
     * GDPR: Send account deletion request confirmation email to business
     */
    @Async
    public void sendBusinessDeletionRequestEmail(String businessEmail, String businessName, LocalDateTime effectiveDate, int futureAppointmentsCount) {
        try {
            String subject = "Demande de suppression de votre compte business";
            String content = buildBusinessDeletionRequestEmail(businessName, effectiveDate, futureAppointmentsCount);

            sendEmail(businessEmail, subject, content);

            log.info("Business deletion request email sent to: {}", businessEmail);
        } catch (Exception e) {
            log.error("Failed to send business deletion request email to {}: {}", businessEmail, e.getMessage(), e);
        }
    }

    /**
     * GDPR: Send deletion cancellation confirmation email to business
     */
    @Async
    public void sendBusinessDeletionCancellationEmail(String businessEmail, String businessName) {
        try {
            String subject = "Annulation de la suppression de votre compte";
            String content = buildBusinessDeletionCancellationEmail(businessName);

            sendEmail(businessEmail, subject, content);

            log.info("Business deletion cancellation email sent to: {}", businessEmail);
        } catch (Exception e) {
            log.error("Failed to send business deletion cancellation email to {}: {}", businessEmail, e.getMessage(), e);
        }
    }

    /**
     * Build business deletion request email content
     */
    private String buildBusinessDeletionRequestEmail(String businessName, LocalDateTime effectiveDate, int futureAppointmentsCount) {
        String effectiveDateStr = effectiveDate.format(DATE_FORMATTER);
        String appointmentsMessage = futureAppointmentsCount > 0
            ? "<div style='background-color: #fffbeb; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0;'>" +
              "  <p style='margin: 0 0 5px 0; font-weight: bold; color: #92400e;'>⚠️ Rendez-vous affectés</p>" +
              "  <p style='margin: 0; color: #78350f; font-size: 14px;'>" +
              "    " + futureAppointmentsCount + " rendez-vous programmé" + (futureAppointmentsCount > 1 ? "s" : "") + " " +
              "    dans les 30 prochains jours " + (futureAppointmentsCount > 1 ? "ont" : "a") + " été annulé" + (futureAppointmentsCount > 1 ? "s" : "") + ". " +
              "    Vos clients ont été notifiés par email." +
              "  </p>" +
              "</div>"
            : "";

        return buildEmailTemplate(
            "Demande de suppression de compte",
            businessName,
            "<div style='background-color: #fee2e2; border-left: 4px solid #ef4444; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 0; color: #991b1b;'><strong>Votre demande de suppression a été enregistrée</strong></p>" +
            "</div>" +
            "<p>Nous avons bien reçu votre demande de suppression de compte business.</p>" +
            "<div style='background-color: #dbeafe; border-left: 4px solid #3b82f6; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 5px 0;'><strong>Date de suppression définitive :</strong> " + effectiveDateStr + "</p>" +
            "  <p style='margin: 5px 0;'><strong>Période de rétractation :</strong> 30 jours</p>" +
            "</div>" +
            appointmentsMessage +
            "<h3 style='color: #1f2937; margin-top: 25px;'>Que se passe-t-il maintenant ?</h3>" +
            "<ul style='color: #4b5563; line-height: 1.8;'>" +
            "  <li>Vous pouvez continuer à vous connecter à votre compte pendant 30 jours</li>" +
            "  <li>Vous pouvez <strong>annuler cette demande à tout moment</strong> depuis votre tableau de bord ou la page RGPD</li>" +
            "  <li>Après 30 jours, votre compte et toutes vos données seront définitivement supprimés</li>" +
            "</ul>" +
            "<div style='background-color: #dcfce7; border-left: 4px solid #16a34a; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 0; color: #166534;'><strong>💡 Vous avez changé d'avis ?</strong></p>" +
            "  <p style='margin: 5px 0 0 0; color: #166534; font-size: 14px;'>" +
            "    Connectez-vous à votre tableau de bord et accédez à la page \"Données & Confidentialité\" pour annuler la suppression." +
            "  </p>" +
            "</div>" +
            "<p style='text-align: center; margin: 30px 0;'>" +
            "  <a href='" + baseUrl + "/business/gdpr' style='display: inline-block; padding: 12px 24px; background-color: #16a34a; color: white; text-decoration: none; border-radius: 6px; font-weight: 600;'>Annuler la suppression</a>" +
            "</p>" +
            "<p style='color: #6b7280; font-size: 14px; margin-top: 30px;'>Si vous n'avez pas demandé cette suppression, veuillez nous contacter immédiatement.</p>"
        );
    }

    /**
     * Build business deletion cancellation email content
     */
    private String buildBusinessDeletionCancellationEmail(String businessName) {
        return buildEmailTemplate(
            "Annulation de la suppression confirmée",
            businessName,
            "<div style='background-color: #dcfce7; border-left: 4px solid #16a34a; padding: 15px; margin: 20px 0;'>" +
            "  <p style='margin: 0; color: #166534;'><strong>✓ Votre compte reste actif !</strong></p>" +
            "  <p style='margin: 5px 0 0 0; color: #166534; font-size: 14px;'>Votre demande de suppression a été annulée avec succès.</p>" +
            "</div>" +
            "<p>Nous sommes ravis de vous garder parmi nous ! Votre compte business est de nouveau actif et tous vos services restent disponibles.</p>" +
            "<h3 style='color: #1f2937; margin-top: 25px;'>Que s'est-il passé ?</h3>" +
            "<ul style='color: #4b5563; line-height: 1.8;'>" +
            "  <li>Votre demande de suppression a été <strong>annulée</strong></li>" +
            "  <li>Votre compte et toutes vos données sont <strong>préservés</strong></li>" +
            "  <li>Vos services restent accessibles à vos clients</li>" +
            "  <li>Vous pouvez continuer à gérer vos rendez-vous normalement</li>" +
            "</ul>" +
            "<p style='text-align: center; margin: 30px 0;'>" +
            "  <a href='" + baseUrl + "/business/dashboard' style='display: inline-block; padding: 12px 24px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 6px; font-weight: 600;'>Accéder à mon tableau de bord</a>" +
            "</p>" +
            "<p style='color: #6b7280; font-size: 14px; margin-top: 30px;'>Merci de continuer à faire confiance à notre plateforme !</p>"
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
