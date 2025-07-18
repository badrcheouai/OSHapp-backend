package com.oshapp.backend.controller;

import com.oshapp.backend.dto.UserCreationRequestDTO;
import com.oshapp.backend.dto.UserResponseDTO;
import com.oshapp.backend.dto.UserUpdateRequestDTO;
import com.oshapp.backend.exception.ResourceNotFoundException;
import com.oshapp.backend.model.ERole;
import com.oshapp.backend.model.Role;
import com.oshapp.backend.model.User;
import com.oshapp.backend.service.NotificationService;
import com.oshapp.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final NotificationService notificationService;

    @PostMapping("/users")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserCreationRequestDTO request) {
        User user = userService.createUser(request.getEmail(), request.getPassword(), request.getRoles(), true);

        // Notifier le RH
        userService.findByRole(ERole.ROLE_RH).forEach(rh ->
                notificationService.createNotification(
                        "Un nouveau compte utilisateur a été créé par l'admin : " + user.getEmail(),
                        rh,
                        "CREATION_COMPTE"
                )
        );

        return ResponseEntity.ok(convertToDto(user));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> userDTOs = userService.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id));
        return ResponseEntity.ok(convertToDto(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequestDTO request) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = userService.findRolesByNames(request.getRoles());
            user.setRoles(roles);
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            userService.updatePassword(user, request.getPassword());
        } else {
            userService.save(user);
        }

        return ResponseEntity.ok(convertToDto(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id));
        userService.delete(user);
        return ResponseEntity.noContent().build();
    }

    private UserResponseDTO convertToDto(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        dto.setActive(user.isActive());
        return dto;
    }
}
 