package com.ohse.OSHapp.controller;

import com.ohse.OSHapp.model.PasswordResetToken;
import com.ohse.OSHapp.repository.PasswordResetTokenRepository;
import com.ohse.OSHapp.controller.KeycloakAdminService;
import com.ohse.OSHapp.controller.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.ConcurrentHashMap;
import com.ohse.OSHapp.model.ActivationCode;
import com.ohse.OSHapp.repository.ActivationCodeRepository;

@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private KeycloakAdminService keycloakAdminService;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private MailService mailService;
    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    // In-memory rate limit: email -> last request time
    private static final ConcurrentHashMap<String, Long> forgotPasswordRateLimit = new ConcurrentHashMap<>();
    // In-memory rate limit: email -> last request time
    private static final ConcurrentHashMap<String, Long> activationRateLimit = new ConcurrentHashMap<>();

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

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        // Rate limit: 1 request per 60 seconds per email
        long now = System.currentTimeMillis();
        Long last = forgotPasswordRateLimit.get(email);
        if (last != null && now - last < 60_000) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Please wait before requesting again.");
        }
        forgotPasswordRateLimit.put(email, now);
        // Remove old tokens for this email
        tokenRepository.deleteByEmail(email);
        // Generate secure token
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);
        PasswordResetToken prt = new PasswordResetToken(email, token, expiry);
        tokenRepository.save(prt);
        // Fetch user info for personalization
        String fullName = "";
        String role = "Utilisateur";
        Optional<KeycloakAdminService.UserInfo> userInfoOpt = keycloakAdminService.getUserInfoByEmail(email);
        if (userInfoOpt.isPresent()) {
            KeycloakAdminService.UserInfo userInfo = userInfoOpt.get();
            fullName = (userInfo.firstName + " " + userInfo.lastName).trim();
            role = userInfo.role;
        }
        // Build reset link (adjust frontend URL as needed)
        String resetLink = "http://localhost:3000/auth/reset-password?token=" + token;
        mailService.sendPasswordResetEmail(email, resetLink, fullName, role);
        return ResponseEntity.ok("Password reset email sent");
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Token and new password are required");
        }
        Optional<PasswordResetToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or expired token");
        }
        PasswordResetToken prt = opt.get();
        if (prt.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(prt);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token expired");
        }
        // Change password in Keycloak
        Optional<String> userIdOpt = keycloakAdminService.findUserIdByEmail(prt.getEmail());
        if (userIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (!PasswordPolicy.isValid(newPassword)) {
            return ResponseEntity.badRequest().body("Le mot de passe ne respecte pas les critères de sécurité : 8 caractères, 1 majuscule, 1 chiffre, 1 caractère spécial.");
        }
        try {
            keycloakAdminService.changePassword(userIdOpt.get(), newPassword);
            tokenRepository.delete(prt);
            return ResponseEntity.ok("Password reset successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not reset password");
        }
    }

    @PostMapping("/send-activation-code")
    @Transactional
    public ResponseEntity<?> sendActivationCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        // Rate limit: 1 request per 60 seconds per email
        long now = System.currentTimeMillis();
        Long last = activationRateLimit.get(email);
        if (last != null && now - last < 60_000) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Please wait before requesting a new code.");
        }
        activationRateLimit.put(email, now);
        // Generate code and upsert in one atomic step
        SecureRandom random = new SecureRandom();
        int codeInt = 100000 + random.nextInt(900000); // 6-digit code
        String code = String.valueOf(codeInt);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        activationCodeRepository.upsertActivationCode(email, code, expiresAt);

        // Send email (existing logic)
        System.out.println("[ACTIVATION] Generated and saved code: " + code + " for email: " + email);
        mailService.sendActivationEmail(email, null, code);
        return ResponseEntity.ok("Activation code sent");
    }

    @PostMapping("/verify-activation-code")
    @Transactional
    public ResponseEntity<?> verifyActivationCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        System.out.println("[ACTIVATION] Attempt to verify code: " + code + " for email: " + email);
        if (email == null || email.isEmpty() || code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("Email and code are required");
        }
        Optional<ActivationCode> opt = activationCodeRepository.findByEmail(email);
        if (opt.isEmpty()) {
            System.out.println("[ACTIVATION] No activation code found for email: " + email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or expired code. Please use the most recent code sent to your email.");
        }
        ActivationCode ac = opt.get();
        System.out.println("[ACTIVATION] Expected code for email " + email + ": " + ac.getCode() + ", used: " + ac.isUsed() + ", expires: " + ac.getExpiryDate());
        if (!ac.getCode().equals(code)) {
            System.out.println("[ACTIVATION] Code does not match latest for email: " + email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or expired code. Please use the most recent code sent to your email.");
        }
        if (ac.isUsed() || ac.getExpiryDate().isBefore(LocalDateTime.now())) {
            activationCodeRepository.delete(ac);
            System.out.println("[ACTIVATION] Code expired or already used: " + code + " for email: " + email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Code expired. Please request a new code.");
        }
        // Mark code as used
        ac.setUsed(true);
        activationCodeRepository.save(ac);
        // Set user as activated (Keycloak: set emailVerified=true)
        Optional<String> userIdOpt = keycloakAdminService.findUserIdByEmail(email);
        if (userIdOpt.isEmpty()) {
            System.out.println("[ACTIVATION] User not found for email: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        try {
            keycloakAdminService.setEmailVerified(userIdOpt.get(), true);
            System.out.println("[ACTIVATION] Account activated for email: " + email);
            return ResponseEntity.ok("Account activated");
        } catch (Exception e) {
            System.out.println("[ACTIVATION] Could not activate account for email: " + email + ", reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not activate account");
        }
    }
} 