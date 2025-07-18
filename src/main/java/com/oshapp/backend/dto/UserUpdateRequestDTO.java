package com.oshapp.backend.dto;

import java.util.Set;
import lombok.Data;

@Data
public class UserUpdateRequestDTO {
    private String email;
    private Set<String> roles;
    private Boolean active;
    private String password; // Optionnel, si l'admin veut réinitialiser le mot de passe
}