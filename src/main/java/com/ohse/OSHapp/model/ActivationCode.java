package com.ohse.OSHapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class ActivationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String code;
    private LocalDateTime expiryDate;
    private boolean used = false;

    public ActivationCode() {}

    public ActivationCode(String email, String code, LocalDateTime expiryDate) {
        this.email = email;
        this.code = code;
        this.expiryDate = expiryDate;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
} 