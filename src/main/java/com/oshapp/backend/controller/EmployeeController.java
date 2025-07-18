package com.oshapp.backend.controller;

import com.oshapp.backend.model.Employee;
import com.oshapp.backend.model.User;
import com.oshapp.backend.service.EmployeeService;
import com.oshapp.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final UserService userService;

    // Récupérer le profil de l'employé connecté
    @GetMapping("/profile")
    public ResponseEntity<Employee> getCurrentEmployeeProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Employee> employee = employeeService.findByUserId(user.get().getId());
        return employee.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Mettre à jour le profil de l'employé
    @PutMapping("/profile")
    public ResponseEntity<Employee> updateEmployeeProfile(@RequestBody Employee employee) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        employee.setUserId(user.get().getId());
        Employee updatedEmployee = employeeService.save(employee);
        return ResponseEntity.ok(updatedEmployee);
    }

    // Récupérer la fiche d'aptitude de l'employé
    @GetMapping("/medical-fitness")
    public ResponseEntity<Map<String, Object>> getMedicalFitness() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Employee> employee = employeeService.findByUserId(user.get().getId());
        if (employee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Simuler une fiche d'aptitude (à remplacer par la vraie logique)
        Map<String, Object> fitnessData = Map.of(
            "employeeId", employee.get().getId(),
            "employeeName", employee.get().getFirstName() + " " + employee.get().getLastName(),
            "fitnessStatus", "APT",
            "lastMedicalVisit", "2024-01-15",
            "nextMedicalVisit", "2024-07-15",
            "restrictions", List.of(),
            "recommendations", List.of("Contrôle annuel recommandé")
        );

        return ResponseEntity.ok(fitnessData);
    }

    // Demander une visite médicale spontanée
    @PostMapping("/medical-visit-request")
    public ResponseEntity<Map<String, Object>> requestMedicalVisit(@RequestBody Map<String, Object> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Simuler la création d'une demande de visite
        Map<String, Object> response = Map.of(
            "requestId", System.currentTimeMillis(),
            "status", "PENDING",
            "message", "Demande de visite médicale envoyée avec succès"
        );

        return ResponseEntity.status(201).body(response);
    }

    // Récupérer les rendez-vous de l'employé
    @GetMapping("/appointments")
    public ResponseEntity<List<Map<String, Object>>> getEmployeeAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Simuler des rendez-vous (à remplacer par la vraie logique)
        List<Map<String, Object>> appointments = List.of(
            Map.of(
                "id", 1,
                "date", "2024-03-15T14:30:00",
                "type", "VISITE_PERIODIQUE",
                "status", "CONFIRMED",
                "doctor", "Dr. Ahmed Zahra",
                "location", "Infirmerie"
            ),
            Map.of(
                "id", 2,
                "date", "2024-04-20T10:00:00",
                "type", "VISITE_SPONTANEE",
                "status", "PENDING",
                "doctor", "Dr. Fatima Benali",
                "location", "Infirmerie"
            )
        );

        return ResponseEntity.ok(appointments);
    }

    // Confirmer un rendez-vous
    @PutMapping("/appointments/{appointmentId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmAppointment(@PathVariable Long appointmentId) {
        // Simuler la confirmation
        Map<String, Object> response = Map.of(
            "appointmentId", appointmentId,
            "status", "CONFIRMED",
            "message", "Rendez-vous confirmé avec succès"
        );

        return ResponseEntity.ok(response);
    }

    // Reporter un rendez-vous
    @PutMapping("/appointments/{appointmentId}/reschedule")
    public ResponseEntity<Map<String, Object>> rescheduleAppointment(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, Object> request) {
        
        // Simuler le report
        Map<String, Object> response = Map.of(
            "appointmentId", appointmentId,
            "status", "RESCHEDULED",
            "message", "Rendez-vous reporté avec succès"
        );

        return ResponseEntity.ok(response);
    }

    // Annuler un rendez-vous
    @PutMapping("/appointments/{appointmentId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, Object> request) {
        
        // Simuler l'annulation
        Map<String, Object> response = Map.of(
            "appointmentId", appointmentId,
            "status", "CANCELLED",
            "message", "Rendez-vous annulé avec succès"
        );

        return ResponseEntity.ok(response);
    }

    // Récupérer les notifications de l'employé
    @GetMapping("/notifications")
    public ResponseEntity<List<Map<String, Object>>> getEmployeeNotifications() {
        // Simuler des notifications
        List<Map<String, Object>> notifications = List.of(
            Map.of(
                "id", 1,
                "title", "Visite médicale programmée",
                "message", "Votre visite médicale est confirmée pour le 15 Mars 2024 à 14h30",
                "type", "APPOINTMENT",
                "read", false,
                "createdAt", "2024-03-10T09:00:00"
            ),
            Map.of(
                "id", 2,
                "title", "Certificat médical à renouveler",
                "message", "Votre certificat médical expire le 20 Mars 2024",
                "type", "REMINDER",
                "read", true,
                "createdAt", "2024-03-08T14:30:00"
            )
        );

        return ResponseEntity.ok(notifications);
    }

    // Marquer une notification comme lue
    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(@PathVariable Long notificationId) {
        // Simuler le marquage comme lu
        Map<String, Object> response = Map.of(
            "notificationId", notificationId,
            "status", "READ",
            "message", "Notification marquée comme lue"
        );

        return ResponseEntity.ok(response);
    }

    // Récupérer les statistiques de l'employé
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEmployeeStats() {
        // Simuler des statistiques
        Map<String, Object> stats = Map.of(
            "totalAppointments", 5,
            "completedAppointments", 3,
            "pendingAppointments", 2,
            "lastMedicalVisit", "2024-01-15",
            "nextMedicalVisit", "2024-07-15",
            "unreadNotifications", 1
        );

        return ResponseEntity.ok(stats);
    }
} 