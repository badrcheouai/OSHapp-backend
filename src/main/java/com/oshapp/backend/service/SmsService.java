package com.oshapp.backend.service;

import com.oshapp.backend.model.Appointment;
import com.oshapp.backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    @Value("${app.notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.notification.sms.provider:mock}")
    private String smsProvider;

    public void sendAppointmentNotification(User user, Appointment appointment) {
        if (!smsEnabled) {
            log.info("SMS notifications disabled, skipping SMS to: {}", user.getPhone());
            return;
        }

        try {
            String message = buildAppointmentSmsContent(user, appointment);
            sendSms(user.getPhone(), message);
            log.info("SMS notification sent to: {}", user.getPhone());
        } catch (Exception e) {
            log.error("Failed to send SMS notification to: {}", user.getPhone(), e);
        }
    }

    public void sendAppointmentStatusNotification(User user, Appointment appointment) {
        if (!smsEnabled) {
            log.info("SMS notifications disabled, skipping status SMS to: {}", user.getPhone());
            return;
        }

        try {
            String message = buildAppointmentStatusSmsContent(user, appointment);
            sendSms(user.getPhone(), message);
            log.info("SMS status notification sent to: {}", user.getPhone());
        } catch (Exception e) {
            log.error("Failed to send status SMS notification to: {}", user.getPhone(), e);
        }
    }

    public void sendObligatoryAppointmentNotification(User user, Appointment appointment) {
        if (!smsEnabled) {
            log.info("SMS notifications disabled, skipping obligatory appointment SMS to: {}", user.getPhone());
            return;
        }

        try {
            String message = buildObligatoryAppointmentSmsContent(user, appointment);
            sendSms(user.getPhone(), message);
            log.info("Obligatory appointment SMS sent to: {}", user.getPhone());
        } catch (Exception e) {
            log.error("Failed to send obligatory appointment SMS to: {}", user.getPhone(), e);
        }
    }

    private void sendSms(String phoneNumber, String message) {
        // Implémentation selon le provider SMS
        switch (smsProvider.toLowerCase()) {
            case "twilio":
                sendViaTwilio(phoneNumber, message);
                break;
            case "nexmo":
                sendViaNexmo(phoneNumber, message);
                break;
            case "mock":
            default:
                sendViaMock(phoneNumber, message);
                break;
        }
    }

    private void sendViaTwilio(String phoneNumber, String message) {
        // Implémentation Twilio
        log.info("Sending SMS via Twilio to: {} - Message: {}", phoneNumber, message);
        // TODO: Implémenter l'intégration Twilio
    }

    private void sendViaNexmo(String phoneNumber, String message) {
        // Implémentation Nexmo
        log.info("Sending SMS via Nexmo to: {} - Message: {}", phoneNumber, message);
        // TODO: Implémenter l'intégration Nexmo
    }

    private void sendViaMock(String phoneNumber, String message) {
        // Mock SMS pour les tests
        log.info("MOCK SMS to: {} - Message: {}", phoneNumber, message);
    }

    private String buildAppointmentSmsContent(User user, Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        return String.format(
            "OSHapp: Nouveau RDV médical le %s à %s. Lieu: %s. Type: %s",
            appointment.getAppointmentDate().format(dateFormatter),
            appointment.getAppointmentDate().format(timeFormatter),
            appointment.getLocation(),
            appointment.getType()
        );
    }

    private String buildAppointmentStatusSmsContent(User user, Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        String statusMessage = switch (appointment.getStatus()) {
            case CONFIRME -> "confirmé";
            case ANNULE -> "annulé";
            case TERMINE -> "terminé";
            case REPORTE -> "reporté";
            case PROPOSE -> "proposé";
            case DEMANDE -> "reçu";
            default -> "mis à jour";
        };
        
        return String.format(
            "OSHapp: RDV médical %s le %s à %s. Lieu: %s",
            statusMessage,
            appointment.getAppointmentDate().format(dateFormatter),
            appointment.getAppointmentDate().format(timeFormatter),
            appointment.getLocation()
        );
    }

    private String buildObligatoryAppointmentSmsContent(User user, Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        return String.format(
            "OSHapp: Visite médicale OBLIGATOIRE le %s à %s. Lieu: %s. Type: %s. Confirmez votre disponibilité.",
            appointment.getAppointmentDate().format(dateFormatter),
            appointment.getAppointmentDate().format(timeFormatter),
            appointment.getLocation(),
            appointment.getType()
        );
    }
} 