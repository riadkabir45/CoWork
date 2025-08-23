package com.aritra.d.riad.CoWork.service;

import com.aritra.d.riad.CoWork.enumurator.UserRole;
import com.aritra.d.riad.CoWork.enumurator.UserStatus;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;

    /**
     * Sync user from Supabase JWT claims to local database
     */
    public Users syncUserFromJWT(Claims claims) {
        String supabaseUserId = claims.getSubject();
        String email = claims.get("email", String.class);
        
        // Try to find existing user by email or supabase ID
        Optional<Users> existingUser = usersRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            Users user = existingUser.get();
            // Update any missing information from JWT
            updateUserFromJWT(user, claims);
            return usersRepository.save(user);
        } else {
            // Create new user from JWT claims
            return createUserFromJWT(claims);
        }
    }

    /**
     * Create a new user from JWT claims
     */
    private Users createUserFromJWT(Claims claims) {
        Users user = new Users();
        user.setEmail(claims.get("email", String.class));
        
        // Extract user metadata from JWT
        Object userMetadata = claims.get("user_metadata");
        if (userMetadata instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> metadata = (java.util.Map<String, Object>) userMetadata;
            user.setFirstName((String) metadata.get("first_name"));
            user.setLastName((String) metadata.get("last_name"));
        }
        
        // Set default values
        user.setRole(UserRole.REGISTERED);
        user.setStatus(UserStatus.ACTIVE);
        user.setMentorshipEligible(false);
        
        return usersRepository.save(user);
    }

    /**
     * Update existing user with information from JWT
     */
    private void updateUserFromJWT(Users user, Claims claims) {
        // Update any missing information
        Object userMetadata = claims.get("user_metadata");
        if (userMetadata instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> metadata = (java.util.Map<String, Object>) userMetadata;
            
            if (user.getFirstName() == null && metadata.get("first_name") != null) {
                user.setFirstName((String) metadata.get("first_name"));
            }
            if (user.getLastName() == null && metadata.get("last_name") != null) {
                user.setLastName((String) metadata.get("last_name"));
            }
        }
        
        // Upgrade from unregistered to registered if needed
        if (user.getRole() == UserRole.UNREGISTERED) {
            user.setRole(UserRole.REGISTERED);
        }
    }

    /**
     * Find user by email
     */
    public Optional<Users> findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    /**
     * Get user by ID
     */
    public Optional<Users> findById(UUID id) {
        return usersRepository.findById(id);
    }
}
