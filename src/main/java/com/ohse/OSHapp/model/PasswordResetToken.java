package com.ohse.OSHapp.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
public class PasswordResetToken {
    // Getters and setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String token;
    private LocalDateTime expiryDate;

    public PasswordResetToken() {}

    public PasswordResetToken(String email, String token, LocalDateTime expiryDate) {
        this.email = email;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public void setId(Long id) { this.id = id; }

    public void setEmail(String email) { this.email = email; }

    public void setToken(String token) { this.token = token; }

    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
} 