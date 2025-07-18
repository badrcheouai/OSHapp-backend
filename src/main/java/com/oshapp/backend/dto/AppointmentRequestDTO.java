package com.oshapp.backend.dto;

import com.oshapp.backend.model.AppointmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequestDTO {
    
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    private Long nurseId;
    private Long doctorId;

    @NotNull(message = "Appointment type is required")
    private AppointmentType type;

    // La date souhaitée par le salarié ou proposée par l'infirmier
    private LocalDateTime requestedDate;
    private LocalDateTime proposedDate;
    
    // La date finale du RDV (utilisée pour la mise à jour)
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime appointmentDate;

    // Motif de la demande (spontanée) ou du report
    private String motif;

    private String reason;
    private String notes;
    private String location;

    // Pour les visites obligatoires initiées par les RH
    private Boolean isObligatory = false;
}