package com.oshapp.backend.controller;

import com.oshapp.backend.dto.AppointmentRequestDTO;
import com.oshapp.backend.dto.AppointmentResponseDTO;
import com.oshapp.backend.model.Appointment;
import com.oshapp.backend.model.AppointmentStatus;
import com.oshapp.backend.model.User;
import com.oshapp.backend.service.AppointmentService;
import com.oshapp.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment management endpoints")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HR', 'NURSE', 'DOCTOR')")
    @Operation(summary = "Create a new appointment", description = "Create a new appointment with validation")
    public ResponseEntity<AppointmentResponseDTO> createAppointment(
            @Valid @RequestBody AppointmentRequestDTO request,
            Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found with email: " + authentication.getName()));
        AppointmentResponseDTO response = appointmentService.createAppointment(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HR', 'NURSE', 'DOCTOR', 'HSE_MANAGER')")
    @Operation(summary = "Get appointment by ID", description = "Retrieve appointment details by ID")
    public ResponseEntity<AppointmentResponseDTO> getAppointment(@PathVariable Long id) {
        AppointmentResponseDTO appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HR', 'NURSE', 'DOCTOR', 'HSE_MANAGER')")
    @Operation(summary = "Get all appointments", description = "Retrieve all appointments with filtering and pagination")
    public ResponseEntity<Page<AppointmentResponseDTO>> getAllAppointments(
            @Parameter(description = "Filter by status") @RequestParam(required = false) AppointmentStatus status,
            @Parameter(description = "Filter by employee ID") @RequestParam(required = false) Long employeeId,
            @Parameter(description = "Filter by date from") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "Filter by date to") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @Parameter(description = "Pagination and sorting") Pageable pageable,
            Authentication authentication) {
        
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found with email: " + authentication.getName()));
        Page<AppointmentResponseDTO> appointments = appointmentService.getAppointments(
                status, employeeId, dateFrom, dateTo, pageable, currentUser);
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR', 'HR')")
    @Operation(summary = "Update appointment", description = "Update appointment details")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequestDTO request) {
        AppointmentResponseDTO updated = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR', 'HR')")
    @Operation(summary = "Update appointment status", description = "Update appointment status")
    public ResponseEntity<AppointmentResponseDTO> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status,
            @RequestParam(required = false) String notes) {
        AppointmentResponseDTO updated = appointmentService.updateAppointmentStatus(id, status, notes);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'HSE_MANAGER')")
    @Operation(summary = "Delete appointment", description = "Delete appointment by ID")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'NURSE', 'DOCTOR')")
    @Operation(summary = "Get my appointments", description = "Get appointments for current user")
    public ResponseEntity<List<AppointmentResponseDTO>> getMyAppointments(
            @Parameter(description = "Filter by status") @RequestParam(required = false) AppointmentStatus status,
            Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found with email: " + authentication.getName()));
        List<AppointmentResponseDTO> appointments = appointmentService.getMyAppointments(currentUser, status);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'NURSE', 'DOCTOR')")
    @Operation(summary = "Get upcoming appointments", description = "Get upcoming appointments for current user")
    public ResponseEntity<List<AppointmentResponseDTO>> getUpcomingAppointments(
            Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found with email: " + authentication.getName()));
        List<AppointmentResponseDTO> appointments = appointmentService.getUpcomingAppointments(currentUser);
        return ResponseEntity.ok(appointments);
    }

    @PatchMapping("/{id}/propose")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    @Operation(summary = "Propose appointment slot", description = "Nurse or doctor proposes a new appointment slot")
    public ResponseEntity<AppointmentResponseDTO> proposeAppointmentSlot(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequestDTO request,
            Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found"));
        AppointmentResponseDTO response = appointmentService.proposeAppointmentSlot(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    @Operation(summary = "Confirm appointment", description = "Employee confirms the proposed appointment")
    public ResponseEntity<AppointmentResponseDTO> confirmAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        AppointmentResponseDTO response = appointmentService.confirmAppointment(id, notes);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    @Operation(summary = "Reschedule appointment", description = "Employee requests to reschedule appointment with reason")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(
            @PathVariable Long id,
            @RequestParam String motif) {
        AppointmentResponseDTO response = appointmentService.rescheduleAppointment(id, motif);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HR', 'NURSE', 'DOCTOR')")
    @Operation(summary = "Cancel appointment", description = "Cancel an appointment with a reason")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found"));
        AppointmentResponseDTO response = appointmentService.cancelAppointment(id, reason, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/obligatory")
    @PreAuthorize("hasAnyRole('HR')")
    @Operation(summary = "Create obligatory appointments", description = "HR creates obligatory appointments for multiple employees")
    public ResponseEntity<List<AppointmentResponseDTO>> createObligatoryAppointments(
            @RequestBody List<AppointmentRequestDTO> requests,
            Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.oshapp.backend.exception.ResourceNotFoundException("User not found"));
        List<AppointmentResponseDTO> responses = new ArrayList<>();
        for (AppointmentRequestDTO request : requests) {
            responses.add(appointmentService.createAppointment(request, currentUser));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
} 