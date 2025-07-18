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
public class DockerNotificationService {

    private final EmailService emailService;

    public void sendAppointmentNotification(User user, Appointment appointment) {
        log.info("Sending appointment notification to user: {} for appointment: {}", 
                user.getUsername(), appointment.getId());
        
        // En mode Docker, on log seulement les notifications
        log.info("Notification: Nouveau rendez-vous médical pour {} - Date: {}", 
                user.getUsername(), appointment.getAppointmentDate());
        
        // Si l'email est activé, on l'envoie
        try {
            emailService.sendAppointmentNotification(user, appointment);
        } catch (Exception e) {
            log.warn("Email service not available, notification logged only: {}", e.getMessage());
        }
    }

    public void sendAppointmentStatusNotification(User user, Appointment appointment) {
        log.info("Sending status notification to user: {} for appointment: {} - Status: {}", 
                user.getUsername(), appointment.getId(), appointment.getStatus());
        
        // En mode Docker, on log seulement les notifications
        log.info("Notification: Mise à jour du rendez-vous pour {} - Statut: {}", 
                user.getUsername(), appointment.getStatus());
        
        // Si l'email est activé, on l'envoie
        try {
            emailService.sendAppointmentStatusNotification(user, appointment);
        } catch (Exception e) {
            log.warn("Email service not available, notification logged only: {}", e.getMessage());
        }
    }

    public void sendObligatoryAppointmentNotification(User user, Appointment appointment) {
        log.info("Sending obligatory appointment notification to user: {} for appointment: {}", 
                user.getUsername(), appointment.getId());
        
        // En mode Docker, on log seulement les notifications
        log.info("Notification: Visite médicale obligatoire pour {} - Date: {}", 
                user.getUsername(), appointment.getAppointmentDate());
        
        // Si l'email est activé, on l'envoie
        try {
            emailService.sendObligatoryAppointmentNotification(user, appointment);
        } catch (Exception e) {
            log.warn("Email service not available, notification logged only: {}", e.getMessage());
        }
    }

    public void sendBulkObligatoryAppointments(List<User> users, List<Appointment> appointments) {
        log.info("Sending bulk obligatory appointments notifications to {} users", users.size());
        
        // En mode Docker, on log seulement les notifications
        for (int i = 0; i < users.size() && i < appointments.size(); i++) {
            User user = users.get(i);
            Appointment appointment = appointments.get(i);
            log.info("Notification: Visite médicale obligatoire pour {} - Date: {}", 
                    user.getUsername(), appointment.getAppointmentDate());
        }
        
        // Si l'email est activé, on l'envoie
        try {
            emailService.sendBulkObligatoryAppointments(users, appointments);
        } catch (Exception e) {
            log.warn("Email service not available, notifications logged only: {}", e.getMessage());
        }
    }
} 