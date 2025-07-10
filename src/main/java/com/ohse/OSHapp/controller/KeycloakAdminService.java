package com.ohse.OSHapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

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