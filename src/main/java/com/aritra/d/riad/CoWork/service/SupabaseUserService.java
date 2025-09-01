package com.aritra.d.riad.CoWork.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SupabaseUserService {

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.anon-key}")
    private String anonKey;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void getAllUsers() {
        try {
            String url = supabaseUrl + "/auth/v1/admin/users";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + anonKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseArray = objectMapper.readTree(response.getBody());
                System.out.println(responseArray);
            }
            
        } catch (Exception e) {
            System.err.println("Error getting user roles from Supabase: " + e.getMessage());
        }
        
    }
}