package com.ohse.OSHapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
        @AuthenticationPrincipal Jwt principal,
        @RequestBody PasswordChangeRequest req
    ) {
        String userId = principal.getSubject(); // JWT sub (UUID)
        String username = principal.getClaimAsString("preferred_username");
        String currentPassword = req.getCurrentPassword();
        String newPassword = req.getNewPassword();

        // 0. Validate input
        if (currentPassword == null || currentPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Current and new password are required");
        }
        // 1. Prevent changing to the same password
        if (currentPassword.equals(newPassword)) {
            return ResponseEntity.badRequest().body("New password must be different from the current password");
        }
        // 2. Verify current password with Keycloak (try login)
        boolean valid;
        try {
            valid = keycloakAdminService.verifyPassword(username, currentPassword);
        } catch (Exception e) {
            // Log and return generic error
            System.err.println("[SECURITY] Password verification error for user: " + username + ", reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not verify current password");
        }
        if (!valid) {
            // Log failed attempt
            System.err.println("[SECURITY] Invalid current password for user: " + username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Current password incorrect");
        }
        // 3. Enforce new password policy
        if (!PasswordPolicy.isValid(newPassword)) {
            return ResponseEntity.badRequest().body("New password does not meet requirements");
        }
        // 4. Change password in Keycloak
        try {
            keycloakAdminService.changePassword(userId, newPassword);
        } catch (Exception e) {
            // Log and return generic error
            System.err.println("[SECURITY] Password change error for user: " + username + ", reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not change password");
        }
        // 5. Log success
        System.out.println("[SECURITY] Password changed for user: " + username);
        return ResponseEntity.ok("Password changed");
    }
} 