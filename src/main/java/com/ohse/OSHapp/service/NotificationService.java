package com.ohse.OSHapp.service;

import com.ohse.OSHapp.model.Notification;
import com.ohse.OSHapp.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String userId) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        return notificationRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, threeDaysAgo);
    }

    public long countUnreadNotifications(String userId) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        return notificationRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, threeDaysAgo)
            .stream().filter(n -> !n.isRead()).count();
    }

    public void markAsRead(Long id) {
        Notification notif = notificationRepository.findById(id).orElseThrow();
        notif.setRead(true);
        notificationRepository.save(notif);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    public void clearAllNotifications(String userId) {
        notificationRepository.deleteAll(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }
} 