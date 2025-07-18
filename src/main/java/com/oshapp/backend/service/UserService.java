package com.oshapp.backend.service;

import com.oshapp.backend.model.ERole;
import com.oshapp.backend.model.Role;
import com.oshapp.backend.model.User;
import com.oshapp.backend.repository.RoleRepository;
import com.oshapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(String email, String password, Set<String> roleNames, boolean active) {
        Set<Role> roles = findRolesByNames(roleNames);
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(roles)
                .active(active)
                .build();
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByRole(ERole role) {
        Role roleEntity = roleRepository.findByName(role).orElseThrow(() -> new RuntimeException("Role not found: " + role));
        return userRepository.findByRolesContaining(roleEntity);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public Set<Role> findRolesByNames(Set<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> roleRepository.findByName(ERole.valueOf(roleName))
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found: " + roleName)))
                .collect(Collectors.toSet());
    }

} 