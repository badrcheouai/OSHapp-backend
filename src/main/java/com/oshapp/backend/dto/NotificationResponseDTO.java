package com.oshapp.backend.dto;

import com.oshapp.backend.model.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponseDTO {
    
    private Long id;
    
    private String title;
    
    private String message;
    
    private NotificationType type;
    
    private boolean read;
    
    private String relatedEntityType;
    
    private Long relatedEntityId;
    
    private LocalDateTime createdAt;
} 