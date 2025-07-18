package com.oshapp.backend.service;

import com.oshapp.backend.dto.AppointmentRequestDTO;
import com.oshapp.backend.dto.AppointmentResponseDTO;
import com.oshapp.backend.model.Appointment;
import com.oshapp.backend.model.AppointmentStatus;
import com.oshapp.backend.model.Employee;
import com.oshapp.backend.model.User;
import com.oshapp.backend.model.ERole;
import com.oshapp.backend.exception.ResourceNotFoundException;
import com.oshapp.backend.exception.UnauthorizedException;
import com.oshapp.backend.model.AppointmentType;
import com.oshapp.backend.repository.AppointmentRepository;
import com.oshapp.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;
    private final MultiChannelNotificationService multiChannelNotificationService;
    private final UserService userService;

    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO request, User currentUser) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Check if user has permission to create appointment for this employee
        if (!hasPermissionToCreateAppointment(currentUser, employee)) {
            throw new UnauthorizedException("You don't have permission to create appointment for this employee");
        }

        Appointment appointment = new Appointment();
        appointment.setEmployee(employee);
        appointment.setType(request.getType());
        appointment.setStatus(AppointmentStatus.DEMANDE);
        appointment.setRequestedDate(request.getRequestedDate()); // Date souhaitée par le salarié
        appointment.setMotif(request.getMotif());
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());
        appointment.setLocation(request.getLocation());
        appointment.setIsObligatory(request.getIsObligatory());
        appointment.setCreatedBy(currentUser.getEmail());
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Notifier tous les acteurs concernés via la méthode factorisée
        List<User> actorsToNotify = getAllActorsToNotify(savedAppointment);
        multiChannelNotificationService.notifyUsers(actorsToNotify, savedAppointment, "CREATION", null);

        return mapToResponseDTO(savedAppointment);
    }

    public AppointmentResponseDTO getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        return mapToResponseDTO(appointment);
    }

    public Page<AppointmentResponseDTO> getAppointments(AppointmentStatus status, Long employeeId, 
            LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable, User currentUser) {
        
        Page<Appointment> appointments;
        
        if (userHasRole(currentUser, ERole.ROLE_SALARIE)) {
            // Employees can only see their own appointments
            appointments = appointmentRepository.findByEmployeeUserId(currentUser.getId(), pageable);
        } else if (userHasRole(currentUser, ERole.ROLE_INFIRMIER) || userHasRole(currentUser, ERole.ROLE_MEDECIN)) {
            // Medical staff can see appointments they're involved with
            appointments = appointmentRepository.findByMedicalStaff(currentUser.getId(), pageable);
        } else {
            // HR and HSE managers can see all appointments with filters
            appointments = appointmentRepository.findWithFilters(status, employeeId, dateFrom, dateTo, pageable);
        }

        return appointments.map(this::mapToResponseDTO);
    }

    public AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        appointment.setEmployee(employee);
        appointment.setType(request.getType());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());
        appointment.setLocation(request.getLocation());
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return mapToResponseDTO(savedAppointment);
    }

    public AppointmentResponseDTO updateAppointmentStatus(Long id, AppointmentStatus status, String notes) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        appointment.setStatus(status);
        if (notes != null && !notes.trim().isEmpty()) {
            appointment.setNotes(appointment.getNotes() + "\n" + notes);
        }
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Notifier tous les acteurs concernés via la méthode factorisée
        List<User> actorsToNotify = getAllActorsToNotify(savedAppointment);
        multiChannelNotificationService.notifyUsers(actorsToNotify, savedAppointment, "STATUS_UPDATE", null);

        return mapToResponseDTO(savedAppointment);
    }

    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Appointment not found");
        }
        appointmentRepository.deleteById(id);
    }

    public List<AppointmentResponseDTO> getMyAppointments(User currentUser, AppointmentStatus status) {
        List<Appointment> appointments;
        
        if (status != null) {
            appointments = appointmentRepository.findByEmployeeUserIdAndStatus(currentUser.getId(), status);
        } else {
            appointments = appointmentRepository.findByEmployeeUserId(currentUser.getId());
        }

        return appointments.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getUpcomingAppointments(User currentUser) {
        List<Appointment> appointments = appointmentRepository.findUpcomingByEmployeeUserId(
                currentUser.getId(), LocalDateTime.now());
        
        return appointments.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Méthode pour proposer un créneau (infirmier/médecin)
    public AppointmentResponseDTO proposeAppointmentSlot(Long appointmentId, AppointmentRequestDTO request, User currentUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

                if (!userHasRole(currentUser, ERole.ROLE_INFIRMIER) && !userHasRole(currentUser, ERole.ROLE_MEDECIN)) {
            throw new UnauthorizedException("Only nurses and doctors can propose appointment slots");
        }

        appointment.setProposedDate(request.getProposedDate());
        appointment.setLocation(request.getLocation());
        appointment.setStatus(AppointmentStatus.PROPOSE);
        appointment.setUpdatedAt(LocalDateTime.now());

                if (userHasRole(currentUser, ERole.ROLE_INFIRMIER)) {
            appointment.setNurse(currentUser);
                } else if (userHasRole(currentUser, ERole.ROLE_MEDECIN)) {
            appointment.setDoctor(currentUser);
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Notifier le salarié et N+1, N+2 de la proposition via multi-canal
        List<User> usersToNotify = new ArrayList<>();
        if (savedAppointment.getEmployee() != null && savedAppointment.getEmployee().getUser() != null) {
            usersToNotify.add(savedAppointment.getEmployee().getUser());
        }
        if (savedAppointment.getEmployee() != null && savedAppointment.getEmployee().getManager1() != null 
            && savedAppointment.getEmployee().getManager1().getUser() != null) {
            usersToNotify.add(savedAppointment.getEmployee().getManager1().getUser());
        }
        if (savedAppointment.getEmployee() != null && savedAppointment.getEmployee().getManager2() != null 
            && savedAppointment.getEmployee().getManager2().getUser() != null) {
            usersToNotify.add(savedAppointment.getEmployee().getManager2().getUser());
        }

        // Notifier le salarié
        if (savedAppointment.getEmployee() != null && savedAppointment.getEmployee().getUser() != null) {
            multiChannelNotificationService.sendAppointmentStatusNotification(savedAppointment.getEmployee().getUser(), savedAppointment);
        }

        // Notifier les managers
        List<User> managers = new ArrayList<>();
        if (savedAppointment.getEmployee() != null && savedAppointment.getEmployee().getManager1() != null 
            && savedAppointment.getEmployee().getManager1().getUser() != null) {
            managers.add(savedAppointment.getEmployee().getManager1().getUser());
        }
        if (savedAppointment.getEmployee() != null && savedAppointment.getEmployee().getManager2() != null 
            && savedAppointment.getEmployee().getManager2().getUser() != null) {
            managers.add(savedAppointment.getEmployee().getManager2().getUser());
        }
        multiChannelNotificationService.notifyManagersOfProposal(savedAppointment, managers);

        return mapToResponseDTO(savedAppointment);
    }

    // Méthode pour confirmer un rendez-vous (salarié)
    public AppointmentResponseDTO confirmAppointment(Long appointmentId, String notes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // La date de RDV finale est la date qui avait été proposée
        if (appointment.getProposedDate() == null) {
            throw new IllegalStateException("Cannot confirm an appointment without a proposed date.");
        }
        appointment.setAppointmentDate(appointment.getProposedDate());
        appointment.setStatus(AppointmentStatus.CONFIRME);
        
        if (notes != null && !notes.trim().isEmpty()) {
            String currentNotes = appointment.getNotes() == null ? "" : appointment.getNotes();
            appointment.setNotes(currentNotes + "\nConfirmation notes: " + notes);
        }
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Notifier tous les acteurs de la confirmation via la méthode factorisée
        List<User> actorsToNotify = getAllActorsToNotify(savedAppointment);
        multiChannelNotificationService.notifyUsers(actorsToNotify, savedAppointment, "CONFIRMATION", null);

        return mapToResponseDTO(savedAppointment);
    }

    // Méthode pour reporter un rendez-vous (salarié)
    public AppointmentResponseDTO rescheduleAppointment(Long appointmentId, String motif) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        appointment.setStatus(AppointmentStatus.REPORTE);
        appointment.setMotif(motif); // Le motif du report est stocké dans le champ motif
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Notifier tous les acteurs du report via la méthode factorisée
        List<User> actorsToNotify = getAllActorsToNotify(savedAppointment);
        multiChannelNotificationService.notifyUsers(actorsToNotify, savedAppointment, "RESCHEDULE", motif);

        return mapToResponseDTO(savedAppointment);
    }

    // Méthode pour annuler un rendez-vous (salarié, RH, etc.)
    public AppointmentResponseDTO cancelAppointment(Long appointmentId, String reason, User currentUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Vérifier les permissions : le salarié peut annuler les siens, RH/Infirmier/Médecin peuvent tout annuler.
        boolean canCancel = false;
        if (userHasRole(currentUser, ERole.ROLE_SALARIE)) {
            if (appointment.getEmployee() != null && appointment.getEmployee().getUser() != null) {
                canCancel = appointment.getEmployee().getUser().getId().equals(currentUser.getId());
            }
        } else if (userHasRole(currentUser, ERole.ROLE_RH) || userHasRole(currentUser, ERole.ROLE_INFIRMIER) || userHasRole(currentUser, ERole.ROLE_MEDECIN)) {
            canCancel = true;
        }

        if (!canCancel) {
            throw new UnauthorizedException("You do not have permission to cancel this appointment.");
        }

        // On ne peut pas annuler un RDV déjà terminé
        if (appointment.getStatus() == AppointmentStatus.TERMINE) {
            throw new IllegalStateException("Cannot cancel a completed appointment.");
        }

        appointment.setStatus(AppointmentStatus.ANNULE);
        if (reason != null && !reason.trim().isEmpty()) {
            String currentNotes = appointment.getNotes() == null ? "" : appointment.getNotes();
            appointment.setNotes(currentNotes + "\nMotif d'annulation: " + reason);
        }
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Notifier tous les acteurs de l'annulation
        List<User> actorsToNotify = getAllActorsToNotify(savedAppointment);
        multiChannelNotificationService.notifyUsers(actorsToNotify, savedAppointment, "CANCEL", reason);

        return mapToResponseDTO(savedAppointment);
    }

    private boolean hasPermissionToCreateAppointment(User currentUser, Employee employee) {
        // Employees can only create appointments for themselves
        if (userHasRole(currentUser, ERole.ROLE_SALARIE)) {
            return employee.getUser().getId().equals(currentUser.getId());
        }
        
        // HR, nurses, and doctors can create appointments for any employee
        return userHasRole(currentUser, ERole.ROLE_RH) || 
               userHasRole(currentUser, ERole.ROLE_INFIRMIER) || 
               userHasRole(currentUser, ERole.ROLE_MEDECIN);
    }

    private AppointmentResponseDTO mapToResponseDTO(Appointment appointment) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(appointment.getId());

        if (appointment.getEmployee() != null) {
            dto.setEmployeeId(appointment.getEmployee().getId());
            dto.setEmployeeName(appointment.getEmployee().getFirstName() + " " + appointment.getEmployee().getLastName());
            if (appointment.getEmployee().getUser() != null) {
                dto.setEmployeeEmail(appointment.getEmployee().getUser().getEmail());
            }
        }

        if (appointment.getNurse() != null) {
            dto.setNurseId(appointment.getNurse().getId());
            dto.setNurseName(appointment.getNurse().getUsername()); // Assuming username is the name
        }

        if (appointment.getDoctor() != null) {
            dto.setDoctorId(appointment.getDoctor().getId());
            dto.setDoctorName(appointment.getDoctor().getUsername()); // Assuming username is the name
        }

        dto.setType(appointment.getType());
        dto.setStatus(appointment.getStatus());
        dto.setRequestedDate(appointment.getRequestedDate());
        dto.setProposedDate(appointment.getProposedDate());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setMotif(appointment.getMotif());
        dto.setReason(appointment.getReason());
        dto.setNotes(appointment.getNotes());
        dto.setLocation(appointment.getLocation());
        dto.setObligatory(appointment.getIsObligatory());
        dto.setCreatedBy(appointment.getCreatedBy());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        return dto;
    }

        private boolean userHasRole(User user, ERole role) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == role);
    }

    // Ajout de la méthode utilitaire pour récupérer tous les acteurs à notifier
    private List<User> getAllActorsToNotify(Appointment appointment) {
        List<User> users = new ArrayList<>();
        // Salarié concerné
        if (appointment.getEmployee() != null && appointment.getEmployee().getUser() != null)
            users.add(appointment.getEmployee().getUser());
        // Infirmier(ère)
        if (appointment.getNurse() != null)
            users.add(appointment.getNurse());
        // Médecin du travail
        if (appointment.getDoctor() != null)
            users.add(appointment.getDoctor());
        // RH (tous les RH)
                users.addAll(userService.findByRole(ERole.ROLE_RH));
        // N+1
        if (appointment.getEmployee() != null && appointment.getEmployee().getManager1() != null
            && appointment.getEmployee().getManager1().getUser() != null)
            users.add(appointment.getEmployee().getManager1().getUser());
        // N+2
        if (appointment.getEmployee() != null && appointment.getEmployee().getManager2() != null
            && appointment.getEmployee().getManager2().getUser() != null)
            users.add(appointment.getEmployee().getManager2().getUser());
        // Supprimer les doublons et les nulls
        return users.stream().filter(Objects::nonNull).distinct().toList();
    }
} 