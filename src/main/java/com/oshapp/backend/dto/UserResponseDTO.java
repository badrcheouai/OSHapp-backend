package com.oshapp.backend.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private Set<String> roles;
    private boolean active;
}