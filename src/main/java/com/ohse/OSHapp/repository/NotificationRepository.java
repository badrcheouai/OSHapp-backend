package com.ohse.OSHapp.repository;

import com.ohse.OSHapp.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(String userId, LocalDateTime after);
} 