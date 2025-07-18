package com.oshapp.backend.service;

import com.oshapp.backend.model.Appointment;
import com.oshapp.backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@oshapp.com}")
    private String fromEmail;

    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;

    public void sendAppointmentNotification(User user, Appointment appointment) {
        if (!emailEnabled) {
            log.info("Email notifications disabled, skipping email to: {}", user.getEmail());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Nouveau rendez-vous médical - OSHapp");
            
            String content = buildAppointmentEmailContent(user, appointment);
            message.setText(content);
            
            mailSender.send(message);
            log.info("Email notification sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email notification to: {}", user.getEmail(), e);
        }
    }

    public void sendAppointmentStatusNotification(User user, Appointment appointment) {
        if (!emailEnabled) {
            log.info("Email notifications disabled, skipping status email to: {}", user.getEmail());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Mise à jour du rendez-vous médical - OSHapp");
            
            String content = buildAppointmentStatusEmailContent(user, appointment);
            message.setText(content);
            
            mailSender.send(message);
            log.info("Email status notification sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send status email notification to: {}", user.getEmail(), e);
        }
    }

    public void sendObligatoryAppointmentNotification(User user, Appointment appointment) {
        if (!emailEnabled) {
            log.info("Email notifications disabled, skipping obligatory appointment email to: {}", user.getEmail());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Visite médicale obligatoire - OSHapp");
            
            String content = buildObligatoryAppointmentEmailContent(user, appointment);
            message.setText(content);
            
            mailSender.send(message);
            log.info("Obligatory appointment email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send obligatory appointment email to: {}", user.getEmail(), e);
        }
    }

    public void sendBulkObligatoryAppointments(List<User> users, List<Appointment> appointments) {
        if (!emailEnabled) {
            log.info("Email notifications disabled, skipping bulk obligatory appointments");
            return;
        }

        for (int i = 0; i < users.size() && i < appointments.size(); i++) {
            sendObligatoryAppointmentNotification(users.get(i), appointments.get(i));
        }
    }

    private String buildAppointmentEmailContent(User user, Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        return String.format("""
            Bonjour %s,
            
            Un nouveau rendez-vous médical a été créé pour vous.
            
            Détails du rendez-vous :
            - Date : %s
            - Heure : %s
            - Lieu : %s
            - Type : %s
            - Raison : %s
            
            Vous recevrez une notification dans l'application dès qu'un créneau vous sera proposé.
            
            Cordialement,
            L'équipe OSHapp
            """,
            user.getUsername(),
            appointment.getAppointmentDate().format(dateFormatter),
            appointment.getAppointmentDate().format(timeFormatter),
            appointment.getLocation(),
            appointment.getType(),
            appointment.getReason()
        );
    }

    private String buildAppointmentStatusEmailContent(User user, Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        String statusMessage = switch (appointment.getStatus()) {
            case CONFIRME -> "Votre rendez-vous a été confirmé";
            case ANNULE -> "Votre rendez-vous a été annulé";
            case TERMINE -> "Votre rendez-vous a été marqué comme terminé";
            case REPORTE -> "Votre rendez-vous a été reporté";
            case PROPOSE -> "Un nouveau créneau vous a été proposé";
            case DEMANDE -> "Votre demande de rendez-vous a été reçue";
            default -> "Le statut de votre rendez-vous a été mis à jour";
        };
        
        return String.format("""
            Bonjour %s,
            
            %s.
            
            Détails du rendez-vous :
            - Date : %s
            - Heure : %s
            - Lieu : %s
            - Type : %s
            - Statut : %s
            
            %s
            
            Cordialement,
            L'équipe OSHapp
            """,
            user.getUsername(),
            statusMessage,
            appointment.getAppointmentDate().format(dateFormatter),
            appointment.getAppointmentDate().format(timeFormatter),
            appointment.getLocation(),
            appointment.getType(),
            appointment.getStatus(),
            appointment.getNotes() != null ? "Notes : " + appointment.getNotes() : ""
        );
    }

    private String buildObligatoryAppointmentEmailContent(User user, Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        return String.format("""
            Bonjour %s,
            
            Une visite médicale obligatoire a été programmée pour vous.
            
            Détails de la visite :
            - Date : %s
            - Heure : %s
            - Lieu : %s
            - Type : %s
            - Raison : %s
            
            IMPORTANT : Cette visite est obligatoire. Veuillez confirmer votre disponibilité 
            ou justifier un éventuel report via l'application OSHapp.
            
            Cordialement,
            L'équipe OSHapp
            """,
            user.getUsername(),
            appointment.getAppointmentDate().format(dateFormatter),
            appointment.getAppointmentDate().format(timeFormatter),
            appointment.getLocation(),
            appointment.getType(),
            appointment.getReason()
        );
    }
} 