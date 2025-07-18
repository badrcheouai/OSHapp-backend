package com.oshapp.backend.dto;

import com.oshapp.backend.model.AppointmentStatus;
import com.oshapp.backend.model.AppointmentType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentResponseDTO {
    
    private Long id;
    
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;

    private Long nurseId;
    private String nurseName;

    private Long doctorId;
    private String doctorName;

    private AppointmentType type;
    private AppointmentStatus status;

    private LocalDateTime requestedDate;
    private LocalDateTime proposedDate;
    private LocalDateTime appointmentDate;

    private String motif;
    private String reason;
    private String notes;
    private String location;
    private boolean isObligatory;

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 