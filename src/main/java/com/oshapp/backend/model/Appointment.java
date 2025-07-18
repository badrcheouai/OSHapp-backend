package com.oshapp.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Date demandée initialement par le salarié
    private LocalDateTime requestedDate;
    
    // Date proposée par l'infirmier/médecin
    private LocalDateTime proposedDate;
    
    // Date finale du rendez-vous
    private LocalDateTime appointmentDate;
    
    @Enumerated(EnumType.STRING)
    private AppointmentType type;
    
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
    
    // Motif de la demande ou du report
    private String motif;
    
    // Raison médicale
    private String reason;
    
    // Notes additionnelles
    private String notes;
    
    // Lieu du rendez-vous
    private String location;
    
    // Indique si c'est une visite obligatoire (initiée par RH)
    private Boolean isObligatory = false;
    
    // Créé par (email de l'utilisateur)
    private String createdBy;
    
    // Dates de création et modification
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "nurse_id")
    private User nurse;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User doctor;
} 