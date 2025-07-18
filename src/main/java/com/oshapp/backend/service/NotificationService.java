package com.oshapp.backend.service;

import com.oshapp.backend.dto.NotificationResponseDTO;
import com.oshapp.backend.model.Appointment;
import com.oshapp.backend.model.Notification;
import com.oshapp.backend.model.User;
import com.oshapp.backend.model.NotificationType;
import com.oshapp.backend.exception.ResourceNotFoundException;
import com.oshapp.backend.exception.UnauthorizedException;
import com.oshapp.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void sendAppointmentNotification(User user, Appointment appointment) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Nouveau rendez-vous");
        notification.setMessage(String.format("Vous avez un nouveau rendez-vous le %s à %s", 
                appointment.getAppointmentDate().toLocalDate(),
                appointment.getAppointmentDate().toLocalTime()));
        notification.setType(NotificationType.APPOINTMENT);
        notification.setRead(false);
        notification.setRelatedEntityType("APPOINTMENT");
        notification.setRelatedEntityId(appointment.getId());
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }

    public void sendAppointmentStatusNotification(User user, Appointment appointment) {
        String statusMessage = switch (appointment.getStatus()) {
            case CONFIRME -> "Votre rendez-vous a été confirmé";
            case ANNULE -> "Votre rendez-vous a été annulé";
            case TERMINE -> "Votre rendez-vous a été marqué comme terminé";
            case REPORTE -> "Votre rendez-vous a été reporté";
            case PROPOSE -> "Un nouveau créneau vous a été proposé";
            case DEMANDE -> "Votre demande de rendez-vous a été reçue";
            default -> "Le statut de votre rendez-vous a été mis à jour";
        };

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Mise à jour du rendez-vous");
        notification.setMessage(statusMessage);
        notification.setType(NotificationType.APPOINTMENT);
        notification.setRead(false);
        notification.setRelatedEntityType("APPOINTMENT");
        notification.setRelatedEntityId(appointment.getId());
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }

    public void sendGeneralNotification(User user, String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }

    public void createNotification(String title, User user, String message) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(NotificationType.INFO);
        notification.setRead(false);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public Page<NotificationResponseDTO> getUserNotifications(User user, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return notifications.map(this::mapToResponseDTO);
    }

    public List<NotificationResponseDTO> getUnreadNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public Long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only mark your own notifications as read");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only delete your own notifications");
        }

        notificationRepository.delete(notification);
    }
        
    private NotificationResponseDTO mapToResponseDTO(Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
} 