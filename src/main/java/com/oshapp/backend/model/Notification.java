package com.oshapp.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;
    private java.time.LocalDateTime createdAt;
    private boolean read;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private String relatedEntityType;
    private Long relatedEntityId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
} 