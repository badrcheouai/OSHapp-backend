package com.ohse.OSHapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@Service
public class KeycloakAdminService {
    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakBaseUrl;
    @Value("${keycloak.realm:oshapp}")
    private String realm;
    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;
    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. Verify current password by logging in as the user (Direct Access Grant)
    public boolean verifyPassword(String username, String currentPassword) {
        String url = keycloakBaseUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String body = "grant_type=password&client_id=oshapp-frontend&username=" + username + "&password=" + currentPassword;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            return res.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    // 2. Change password using Keycloak Admin REST API
    public void changePassword(String userId, String newPassword) {
        String adminToken = getAdminToken();
        String url = keycloakBaseUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        String body = "{" +
                "\"type\":\"password\"," +
                "\"value\":\"" + newPassword + "\"," +
                "\"temporary\":false}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    // Find userId by email using Keycloak Admin REST API
    public Optional<String> findUserIdByEmail(String email) {
        String adminToken = getAdminToken();
        String url = keycloakBaseUrl + "/admin/realms/" + realm + "/users";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("email", email);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Object[]> res = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, Object[].class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null && res.getBody().length > 0) {
                Map user = (Map) res.getBody()[0];
                return Optional.ofNullable((String) user.get("id"));
            }
        } catch (Exception e) {
            // log error if needed
        }
        return Optional.empty();
    }

    public void setEmailVerified(String userId, boolean verified) {
        String adminToken = getAdminToken();
        String url = keycloakBaseUrl + "/admin/realms/" + realm + "/users/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        String body = "{\"emailVerified\": " + verified + "}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    public static class UserInfo {
        public final String firstName;
        public final String lastName;
        public final String role;
        public UserInfo(String firstName, String lastName, String role) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
        }
    }

    private static String mapRoleLabel(String role) {
        switch (role) {
            case "MEDECIN_TRAVAIL": return "Médecin";
            case "INFIRMIER_ST": return "Infirmier";
            case "RESP_RH": return "RH";
            case "RESP_HSE": return "HSE";
            case "SALARIE": return "Salarié";
            case "ADMIN": return "Administrateur";
            default: return role;
        }
    }

    public Optional<UserInfo> getUserInfoByEmail(String email) {
        String adminToken = getAdminToken();
        String url = keycloakBaseUrl + "/admin/realms/" + realm + "/users";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("email", email);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Object[]> res = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, Object[].class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null && res.getBody().length > 0) {
                Map user = (Map) res.getBody()[0];
                String userId = (String) user.get("id");
                String firstName = (String) user.getOrDefault("firstName", "");
                String lastName = (String) user.getOrDefault("lastName", "");
                // Get roles
                String rolesUrl = keycloakBaseUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
                HttpEntity<Void> rolesEntity = new HttpEntity<>(headers);
                ResponseEntity<List> rolesRes = restTemplate.exchange(rolesUrl, HttpMethod.GET, rolesEntity, List.class);
                String role = "Utilisateur";
                if (rolesRes.getStatusCode().is2xxSuccessful() && rolesRes.getBody() != null && !rolesRes.getBody().isEmpty()) {
                    Map roleMap = (Map) rolesRes.getBody().get(0);
                    role = mapRoleLabel((String) roleMap.getOrDefault("name", "Utilisateur"));
                }
                return Optional.of(new UserInfo(firstName, lastName, role));
            }
        } catch (Exception e) {
            // log error if needed
        }
        return Optional.empty();
    }

    // Helper: Get admin token
    private String getAdminToken() {
        String url = keycloakBaseUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String body = "grant_type=password&client_id=admin-cli&username=" + adminUsername + "&password=" + adminPassword;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        if (!res.getStatusCode().is2xxSuccessful()) throw new RuntimeException("Failed to get admin token");
        return (String) res.getBody().get("access_token");
    }
} 