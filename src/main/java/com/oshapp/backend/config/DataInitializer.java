package com.oshapp.backend.config;

import com.oshapp.backend.model.ERole;
import com.oshapp.backend.model.Role;
import com.oshapp.backend.model.User;
import com.oshapp.backend.repository.RoleRepository;
import com.oshapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            log.info("Initializing roles...");
            Arrays.stream(ERole.values()).forEach(eRole -> roleRepository.save(new Role(eRole)));
            log.info("Roles initialized.");
        }

        if (userRepository.count() == 0) {
            log.info("Initializing test users...");
            createTestUsers();
            log.info("Test users initialized.");
        }
    }

    private void createTestUsers() {
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Role rhRole = roleRepository.findByName(ERole.ROLE_RH).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Role infirmierRole = roleRepository.findByName(ERole.ROLE_INFIRMIER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Role medecinRole = roleRepository.findByName(ERole.ROLE_MEDECIN).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Role salarieRole = roleRepository.findByName(ERole.ROLE_SALARIE).orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        User adminUser = User.builder()
                .email("admin@oshapp.com")
                .password(passwordEncoder.encode("password"))
                .roles(new HashSet<>(Set.of(adminRole)))
                .active(true)
                .build();
        userRepository.save(adminUser);

        User rhUser = User.builder()
                .email("rh@oshapp.com")
                .password(passwordEncoder.encode("password"))
                .roles(new HashSet<>(Set.of(rhRole)))
                .active(true)
                .build();
        userRepository.save(rhUser);

        User infirmierUser = User.builder()
                .email("infirmier@oshapp.com")
                .password(passwordEncoder.encode("password"))
                .roles(new HashSet<>(Set.of(infirmierRole)))
                .active(true)
                .build();
        userRepository.save(infirmierUser);

        User medecinUser = User.builder()
                .email("medecin@oshapp.com")
                .password(passwordEncoder.encode("password"))
                .roles(new HashSet<>(Set.of(medecinRole)))
                .active(true)
                .build();
        userRepository.save(medecinUser);

        User salarieUser = User.builder()
                .email("salarie@oshapp.com")
                .password(passwordEncoder.encode("password"))
                .roles(new HashSet<>(Set.of(salarieRole)))
                .active(true)
                .build();
        userRepository.save(salarieUser);
    }
}