package com.aritra.d.riad.CoWork.controller;

import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public String testConnection() {
        return "Backend connection successful!";
    }

    /**
     * Get current user information from JWT token
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        Boolean authenticated = (Boolean) request.getAttribute("authenticated");
        
        if (authenticated == null || !authenticated) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            Claims claims = (Claims) request.getAttribute("userClaims");
            
            // Sync user with database
            Users user = userService.syncUserFromJWT(claims);
            
            // Return user information
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("primaryRole", user.getPrimaryRole());
            response.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .toList());
            response.put("status", user.getStatus());
            response.put("profilePicture", user.getProfilePicture());
            response.put("mentorshipEligible", user.getMentorshipEligible());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get user information"));
        }
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updates, HttpServletRequest request) {
        Boolean authenticated = (Boolean) request.getAttribute("authenticated");
        
        if (authenticated == null || !authenticated) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            Claims claims = (Claims) request.getAttribute("userClaims");
            
            // Get user from database
            Users user = userService.syncUserFromJWT(claims);
            
            // Update fields if provided
            if (updates.containsKey("firstName")) {
                user.setFirstName((String) updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                user.setLastName((String) updates.get("lastName"));
            }
            if (updates.containsKey("bio")) {
                user.setBio((String) updates.get("bio"));
            }
            if (updates.containsKey("location")) {
                user.setLocation((String) updates.get("location"));
            }
            if (updates.containsKey("mentorshipEligible")) {
                user.setMentorshipEligible((Boolean) updates.get("mentorshipEligible"));
            }
            
            // Save updated user
            Users updatedUser = userService.findById(user.getId()).orElse(user);
            
            // Return updated user information
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("email", updatedUser.getEmail());
            response.put("firstName", updatedUser.getFirstName());
            response.put("lastName", updatedUser.getLastName());
            response.put("bio", updatedUser.getBio());
            response.put("location", updatedUser.getLocation());
            response.put("primaryRole", updatedUser.getPrimaryRole());
            response.put("roles", updatedUser.getRoles().stream()
                .map(role -> role.getName())
                .toList());
            response.put("status", updatedUser.getStatus());
            response.put("mentorshipEligible", updatedUser.getMentorshipEligible());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update profile"));
        }
    }
}