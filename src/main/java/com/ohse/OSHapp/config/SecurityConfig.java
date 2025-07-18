package com.ohse.OSHapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
            .cors() // <-- Enable CORS
            .and()
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health").permitAll()
                .requestMatchers("/test-email").permitAll()
                .requestMatchers("/account/forgot-password").permitAll()
                .requestMatchers("/account/reset-password").permitAll()
                .requestMatchers("/api/notifications").permitAll()
                .requestMatchers("/api/notifications/unread-count").permitAll()
                .requestMatchers("/api/notifications/simulate-rendezvous").permitAll()
                .requestMatchers("/api/notifications/*/read").permitAll()
                .requestMatchers("/api/notifications/*").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/rh/**").hasRole("RESP_RH")
                .requestMatchers("/infirmier/**").hasRole("INFIRMIER_ST")
                .requestMatchers("/medecin/**").hasRole("MEDECIN_TRAVAIL")
                .requestMatchers("/hse/**").hasRole("RESP_HSE")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(o2 -> o2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
            );

        return http.build();
    }
}
