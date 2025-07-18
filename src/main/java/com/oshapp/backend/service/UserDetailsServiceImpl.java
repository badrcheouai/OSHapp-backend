package com.oshapp.backend.service;

import com.oshapp.backend.model.User;
import com.oshapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // The 'username' parameter from the login form is actually the email
        log.info("Attempting to load user by email: {}", username);
        Optional<com.oshapp.backend.model.User> user = userRepository.findByEmail(username);

        if (user.isEmpty()) {
            log.error("User not found with email: {}", username);
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        log.info("User found with email: {}. Is active: {}", username, user.get().isActive());
        return user.get();
    }
}
