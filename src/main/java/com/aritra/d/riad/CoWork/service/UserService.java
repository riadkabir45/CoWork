package com.aritra.d.riad.CoWork.service;

import com.aritra.d.riad.CoWork.enumurator.UserStatus;
import com.aritra.d.riad.CoWork.model.Role;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.RoleRepository;
import com.aritra.d.riad.CoWork.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Sync user from Supabase JWT claims to local database
     */
    public Users syncUserFromJWT(Claims claims) {
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
        Set<Role> defaultRoles = new HashSet<>();
        roleRepository.findByName("REGISTERED").ifPresent(defaultRoles::add);
        user.setRoles(defaultRoles);
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
        if (user.getRoles().isEmpty() || user.hasRole("UNREGISTERED")) {
            Set<Role> roles = new HashSet<>(user.getRoles());
            roleRepository.findByName("REGISTERED").ifPresent(roles::add);
            // Remove UNREGISTERED role if present
            roles.removeIf(role -> role.getName().equals("UNREGISTERED"));
            user.setRoles(roles);
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
    
    /**
     * Promote user to mentor role
     */
    public Users promoteToMentor(UUID userId) {
        Optional<Users> userOpt = usersRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        Users user = userOpt.get();
        Set<Role> roles = new HashSet<>(user.getRoles());
        roleRepository.findByName("MENTOR").ifPresent(roles::add);
        user.setRoles(roles);
        user.setMentorshipEligible(true);
        
        return usersRepository.save(user);
    }
    
    /**
     * Assign moderator role to user (admin only)
     */
    public Users assignModerator(UUID userId) {
        Optional<Users> userOpt = usersRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        Users user = userOpt.get();
        Set<Role> roles = new HashSet<>(user.getRoles());
        roleRepository.findByName("MODERATOR").ifPresent(roles::add);
        user.setRoles(roles);
        
        return usersRepository.save(user);
    }
    
    /**
     * Remove moderator role from user (admin only)
     */
    public Users removeModerator(UUID userId) {
        Optional<Users> userOpt = usersRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        Users user = userOpt.get();
        Set<Role> roles = new HashSet<>(user.getRoles());
        roles.removeIf(role -> role.getName().equals("MODERATOR"));
        user.setRoles(roles);
        
        return usersRepository.save(user);
    }
    
    /**
     * Assign admin role to user (for initial setup)
     */
    public Users assignAdmin(UUID userId) {
        Optional<Users> userOpt = usersRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        Users user = userOpt.get();
        Set<Role> roles = new HashSet<>(user.getRoles());
        roleRepository.findByName("ADMIN").ifPresent(roles::add);
        user.setRoles(roles);
        
        return usersRepository.save(user);
    }
    
    /**
     * Assign role to user by role name
     */
    public Users assignRole(UUID userId, String roleName) {
        Optional<Users> userOpt = usersRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        Users user = userOpt.get();
        Set<Role> roles = new HashSet<>(user.getRoles());
        roleRepository.findByName(roleName).ifPresentOrElse(
            roles::add,
            () -> { throw new RuntimeException("Role not found: " + roleName); }
        );
        user.setRoles(roles);
        
        return usersRepository.save(user);
    }
}
