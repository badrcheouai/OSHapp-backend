package com.oshapp.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private String phoneNumber;
    private String position;
    private String department;
    private String employeeId;
    private String cin;
    private String cnss;
    private LocalDate hireDate;
    private LocalDate birthDate;
    private String birthPlace;
    private String address;
    private String maritalStatus;
    private Integer childrenCount;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "manager1_id")
    private Employee manager1; // N+1

    @ManyToOne
    @JoinColumn(name = "manager2_id")
    private Employee manager2; // N+2
    
    // Méthode utilitaire pour obtenir l'ID de l'utilisateur
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    // Méthode utilitaire pour définir l'ID de l'utilisateur
    public void setUserId(Long userId) {
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }
}