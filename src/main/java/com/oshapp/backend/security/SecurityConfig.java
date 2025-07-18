package com.oshapp.backend.security;

import com.oshapp.backend.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;


import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // Important for local dev with cookies/tokens
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Profile("local")
    @Configuration
    @RequiredArgsConstructor
    public static class LocalSecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final UserDetailsServiceImpl userDetailsService;
        private final PasswordEncoder passwordEncoder;

        @Bean
        public AuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(userDetailsService);
            authProvider.setPasswordEncoder(passwordEncoder);
            return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
            return config.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain localFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable()) // Use modern lambda DSL to disable CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityMatcher("/api/**") // Apply this filter chain only to /api/** paths
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/v1/auth/**").permitAll() // Allow auth endpoints
                    .anyRequest().authenticated() // Secure all other API endpoints
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> 
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage()))
                );
            return http.build();
        }
    }

    @Profile("keycloak")
    @Configuration
    @RequiredArgsConstructor
    public static class KeycloakSecurityConfig {

        private final JwtAuthConverter jwtAuthConverter;

        @Bean
        public SecurityFilterChain keycloakFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/auth/**").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/api/v1/auth/**").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/rh/**").hasRole("HR")
                    .requestMatchers("/api/v1/employees/**").hasAnyRole("EMPLOYEE", "HR", "ADMIN")
                    .requestMatchers("/api/v1/nurse/**").hasRole("NURSE")
                    .requestMatchers("/api/v1/doctor/**").hasRole("DOCTOR")
                    .requestMatchers("/api/v1/hse/**").hasRole("HSE")
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(o2 -> o2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );
            return http.build();
        }
    }
}