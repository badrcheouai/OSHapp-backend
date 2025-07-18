package com.oshapp.backend.repository;

import com.oshapp.backend.model.Notification;
import com.oshapp.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    Page<Notification> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(@Param("user") User user);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.read = false")
    Long countByUserAndReadFalse(@Param("user") User user);
} 