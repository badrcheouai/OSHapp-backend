package com.oshapp.backend.service;

import com.oshapp.backend.model.Appointment;
import com.oshapp.backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiChannelNotificationService {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final SmsService smsService;

    /**
     * Envoie une notification multi-canal pour un nouveau rendez-vous
     * Selon le scénario : Application + Email + SMS
     */
    public void sendAppointmentNotification(User user, Appointment appointment) {
        log.info("Sending multi-channel notification for appointment {} to user {}", 
                appointment.getId(), user.getUsername());

        // 1. Notification in-app
        notificationService.sendAppointmentNotification(user, appointment);

        // 2. Email notification
        emailService.sendAppointmentNotification(user, appointment);

        // 3. SMS notification
        smsService.sendAppointmentNotification(user, appointment);
    }

    /**
     * Envoie une notification multi-canal pour un changement de statut
     * Selon le scénario : Application + Email + SMS
     */
    public void sendAppointmentStatusNotification(User user, Appointment appointment) {
        log.info("Sending multi-channel status notification for appointment {} to user {}", 
                appointment.getId(), user.getUsername());

        // 1. Notification in-app
        notificationService.sendAppointmentStatusNotification(user, appointment);

        // 2. Email notification
        emailService.sendAppointmentStatusNotification(user, appointment);

        // 3. SMS notification
        smsService.sendAppointmentStatusNotification(user, appointment);
    }

    /**
     * Envoie une notification multi-canal pour une visite obligatoire
     * Selon le scénario : Application + Email + SMS
     */
    public void sendObligatoryAppointmentNotification(User user, Appointment appointment) {
        log.info("Sending multi-channel obligatory appointment notification for appointment {} to user {}", 
                appointment.getId(), user.getUsername());

        // 1. Notification in-app
        notificationService.sendGeneralNotification(user, 
                "Visite médicale obligatoire", 
                "Une visite médicale obligatoire a été programmée pour vous. Veuillez confirmer votre disponibilité.", 
                com.oshapp.backend.model.NotificationType.APPOINTMENT);

        // 2. Email notification
        emailService.sendObligatoryAppointmentNotification(user, appointment);

        // 3. SMS notification
        smsService.sendObligatoryAppointmentNotification(user, appointment);
    }

    /**
     * Envoie des notifications multi-canal pour des visites obligatoires en lot
     * Utilisé par le RH pour envoyer des listes de salariés
     */
    public void sendBulkObligatoryAppointments(List<User> users, List<Appointment> appointments) {
        log.info("Sending bulk obligatory appointment notifications to {} users", users.size());

        for (int i = 0; i < users.size() && i < appointments.size(); i++) {
            sendObligatoryAppointmentNotification(users.get(i), appointments.get(i));
        }
    }

    /**
     * Notifie tous les acteurs concernés selon le scénario
     * - Infirmier(ère)
     * - Médecin du travail  
     * - Responsable RH
     * - Chef hiérarchique N+1 et N+2
     */
    public void notifyAllActors(Appointment appointment, List<User> actors) {
        log.info("Notifying all actors for appointment {}", appointment.getId());

        for (User actor : actors) {
            if (actor != null) {
                sendAppointmentNotification(actor, appointment);
            }
        }
    }

    /**
     * Notifie spécifiquement les managers N+1 et N+2 d'une proposition de créneau
     * Selon le scénario : les managers peuvent signaler une indisponibilité
     */
    public void notifyManagersOfProposal(Appointment appointment, List<User> managers) {
        log.info("Notifying managers of appointment proposal {}", appointment.getId());

        for (User manager : managers) {
            if (manager != null) {
                notificationService.sendGeneralNotification(manager,
                        "Proposition de créneau médical",
                        String.format("Un créneau médical a été proposé pour %s %s. Vous pouvez signaler une indisponibilité si nécessaire.",
                                appointment.getEmployee().getFirstName(),
                                appointment.getEmployee().getLastName()),
                        com.oshapp.backend.model.NotificationType.VALIDATION);
                
                emailService.sendAppointmentStatusNotification(manager, appointment);
            }
        }
    }

    /**
     * Notifie le salarié et les managers de la confirmation d'un rendez-vous
     */
    public void notifyConfirmation(Appointment appointment, User employee, List<User> managers) {
        log.info("Notifying confirmation for appointment {}", appointment.getId());

        // Notifier le salarié
        if (employee != null) {
            sendAppointmentStatusNotification(employee, appointment);
        }

        // Notifier les managers
        for (User manager : managers) {
            if (manager != null) {
                notificationService.sendGeneralNotification(manager,
                        "Rendez-vous médical confirmé",
                        String.format("Le rendez-vous médical de %s %s a été confirmé.",
                                appointment.getEmployee().getFirstName(),
                                appointment.getEmployee().getLastName()),
                        com.oshapp.backend.model.NotificationType.APPOINTMENT);
            }
        }
    }

    /**
     * Notifie d'un report de rendez-vous avec motif
     */
    public void notifyReschedule(Appointment appointment, User employee, List<User> managers, String motif) {
        log.info("Notifying reschedule for appointment {} with motif: {}", appointment.getId(), motif);

        // Notifier le salarié
        if (employee != null) {
            notificationService.sendGeneralNotification(employee,
                    "Rendez-vous reporté",
                    String.format("Votre rendez-vous médical a été reporté. Motif: %s", motif),
                    com.oshapp.backend.model.NotificationType.APPOINTMENT);
        }

        // Notifier les managers
        for (User manager : managers) {
            if (manager != null) {
                notificationService.sendGeneralNotification(manager,
                        "Rendez-vous médical reporté",
                        String.format("Le rendez-vous médical de %s %s a été reporté. Motif: %s",
                                appointment.getEmployee().getFirstName(),
                                appointment.getEmployee().getLastName(),
                                motif),
                        com.oshapp.backend.model.NotificationType.APPOINTMENT);
            }
        }
    }

    /**
     * Notifie une liste d'utilisateurs selon le scénario métier (factorisation)
     * scenario: "CREATION", "STATUS_UPDATE", "CONFIRMATION", "RESCHEDULE", "OBLIGATORY"
     * extraMessage: message additionnel pour certains scénarios (ex: motif de report)
     */
    public void notifyUsers(List<User> users, Appointment appointment, String scenario, String extraMessage) {
        log.info("Notifying users for scenario {} on appointment {}", scenario, appointment.getId());
        for (User user : users) {
            if (user == null) continue;
            switch (scenario) {
                case "CREATION":
                    sendAppointmentNotification(user, appointment);
                    break;
                case "STATUS_UPDATE":
                    sendAppointmentStatusNotification(user, appointment);
                    break;
                case "CONFIRMATION":
                    sendAppointmentStatusNotification(user, appointment);
                    break;
                case "RESCHEDULE":
                    notificationService.sendGeneralNotification(user,
                        "Rendez-vous reporté",
                        String.format("Votre rendez-vous médical a été reporté. Motif: %s", extraMessage),
                        com.oshapp.backend.model.NotificationType.APPOINTMENT);
                    emailService.sendAppointmentStatusNotification(user, appointment);
                    smsService.sendAppointmentStatusNotification(user, appointment);
                    break;
                case "OBLIGATORY":
                    sendObligatoryAppointmentNotification(user, appointment);
                    break;
                default:
                    log.warn("Unknown notification scenario: {}", scenario);
            }
        }
    }
} 