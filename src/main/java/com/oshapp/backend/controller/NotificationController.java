package com.oshapp.backend.controller;

import com.oshapp.backend.dto.NotificationResponseDTO;
import com.oshapp.backend.model.User;
import com.oshapp.backend.service.NotificationService;
import com.oshapp.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Get all notifications for current user")
    public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(
            Pageable pageable,
            Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found with email: " + authentication.getName()));
        Page<NotificationResponseDTO> notifications = notificationService.getUserNotifications(currentUser, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Get unread notifications for current user")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found with email: " + authentication.getName()));
        List<NotificationResponseDTO> notifications = notificationService.getUnreadNotifications(currentUser);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found with email: " + authentication.getName()));
        Long count = notificationService.getUnreadCount(currentUser);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found with email: " + authentication.getName()));
        notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications as read for current user")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found with email: " + authentication.getName()));
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found with email: " + authentication.getName()));
        notificationService.deleteNotification(id, currentUser);
        return ResponseEntity.noContent().build();
    }
} 