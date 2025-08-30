package com.aritra.d.riad.CoWork.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SupabaseRoleService {

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.anon-key}")
    private String anonKey;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get user roles from Supabase database
     */
    public List<String> getUserRoles(String userId) {
        try {
            String url = supabaseUrl + "/rest/v1/user_roles_view?user_id=eq." + userId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + anonKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseArray = objectMapper.readTree(response.getBody());
                if (responseArray.isArray() && responseArray.size() > 0) {
                    JsonNode userNode = responseArray.get(0);
                    JsonNode rolesNode = userNode.get("roles");
                    if (rolesNode != null && rolesNode.isArray()) {
                        List<String> roles = new ArrayList<>();
                        rolesNode.forEach(roleNode -> roles.add(roleNode.asText()));
                        return roles;
                    }
                }
            }
            
            // Default role if no roles found
            return List.of("REGISTERED");
        } catch (Exception e) {
            System.err.println("Error getting user roles from Supabase: " + e.getMessage());
            return List.of("REGISTERED");
        }
    }

    /**
     * Get user roles from JWT claims (fallback method)
     */
    public List<String> getUserRolesFromJWT(Claims claims) {
        String userId = claims.getSubject();
        
        // First try to get from database
        List<String> dbRoles = getUserRoles(userId);
        if (!dbRoles.isEmpty() && !dbRoles.equals(List.of("REGISTERED"))) {
            return dbRoles;
        }
        
        // Fallback to JWT claims
        try {
            Object appMetadata = claims.get("app_metadata");
            if (appMetadata instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = (Map<String, Object>) appMetadata;
                Object roles = metadata.get("roles");
                if (roles instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> roleList = (List<String>) roles;
                    return roleList;
                }
            }
            
            return List.of("REGISTERED");
        } catch (Exception e) {
            System.err.println("Error extracting roles from JWT: " + e.getMessage());
            return List.of("REGISTERED");
        }
    }

    /**
     * Get primary role (highest priority role)
     */
    public String getPrimaryRole(List<String> roles) {
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("MODERATOR")) return "MODERATOR";
        if (roles.contains("MENTOR")) return "MENTOR";
        if (roles.contains("REGISTERED")) return "REGISTERED";
        return "UNREGISTERED";
    }

    /**
     * Assign role to user
     */
    public boolean assignRole(String userEmail, String roleName) {
        try {
            String url = supabaseUrl + "/rest/v1/rpc/assign_user_role";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + anonKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // First get user ID by email
            String userId = getUserIdByEmail(userEmail);
            if (userId == null) {
                return false;
            }
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("target_user_id", userId);
            requestBody.put("role_name", roleName);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error assigning role: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove role from user
     */
    public boolean removeRole(String userEmail, String roleName) {
        try {
            String url = supabaseUrl + "/rest/v1/rpc/remove_user_role";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + anonKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // First get user ID by email
            String userId = getUserIdByEmail(userEmail);
            if (userId == null) {
                return false;
            }
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("target_user_id", userId);
            requestBody.put("role_name", roleName);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error removing role: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get user ID by email
     */
    private String getUserIdByEmail(String email) {
        try {
            String url = supabaseUrl + "/rest/v1/user_roles_view?email=eq." + email;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + anonKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseArray = objectMapper.readTree(response.getBody());
                if (responseArray.isArray() && responseArray.size() > 0) {
                    JsonNode userNode = responseArray.get(0);
                    JsonNode userIdNode = userNode.get("user_id");
                    if (userIdNode != null) {
                        return userIdNode.asText();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting user ID by email: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all users with their roles
     */
    public List<JsonNode> getAllUsers() {
        try {
            String url = supabaseUrl + "/rest/v1/user_roles_view";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + anonKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseArray = objectMapper.readTree(response.getBody());
                List<JsonNode> users = new ArrayList<>();
                if (responseArray.isArray()) {
                    responseArray.forEach(users::add);
                }
                return users;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error getting all users: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
