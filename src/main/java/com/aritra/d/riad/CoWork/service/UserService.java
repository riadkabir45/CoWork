package com.aritra.d.riad.CoWork.service;

import com.aritra.d.riad.CoWork.dto.SimpleUserDTO;
import com.aritra.d.riad.CoWork.dto.SupabaseUserDTO;
import com.aritra.d.riad.CoWork.enumurator.UserStatus;
import com.aritra.d.riad.CoWork.model.Role;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.RoleRepository;
import com.aritra.d.riad.CoWork.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.tools.JavaFileObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private com.aritra.d.riad.CoWork.repository.UserProfileRepository userProfileRepository;

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.service-role-key}")
    private String supabaseServiceRoleKey;

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
    public Optional<Users> findById(String id) {
        return usersRepository.findById(id);
    }
    
    /**
     * Promote user to mentor role
     */
    public Users promoteToMentor(String userId) {
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
     * Promote user to mentor role
     */
    public Users demoteFromMentor(String userId) {
        Optional<Users> userOpt = usersRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        Users user = userOpt.get();
        Set<Role> roles = new HashSet<>(user.getRoles());
        roleRepository.findByName("MENTOR").ifPresent(roles::remove);
        user.setRoles(roles);
        user.setMentorshipEligible(false);
        
        return usersRepository.save(user);
    }
    
    /**
     * Assign moderator role to user (admin only)
     */
    public Users assignModerator(String userId) {
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
    public Users removeModerator(String userId) {
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
    public Users assignAdmin(String userId) {
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
    public Users assignRole(String userId, String roleName) {
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

    public Users createUser(Users user) {
        return usersRepository.save(user);
    }

    public Users createUser(SupabaseUserDTO dto) {
        Users user = new Users();
        user.setAuthId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        return usersRepository.save(user);
    }

    public Users createUser(String email, String firstName, String lastName) {
        Users user = new Users();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return usersRepository.save(user);
    }

    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    public List<SimpleUserDTO> getAllUsersDTO() {
        List<Users> users = usersRepository.findAll();
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private SimpleUserDTO convertToDTO(Users user) {
        SimpleUserDTO dto = new SimpleUserDTO();
        dto.setId(user.getId().toString());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProfilePicture(user.getProfilePicture());
        return dto;
    }

    /**
     * Pulls user list from Supabase Auth and logs them
     */
    public void loadSupabaseUsers() {
        String url = supabaseUrl + "/auth/v1/admin/users";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseServiceRoleKey);
        headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode users = root.has("users") ? root.get("users") : root;
                if (users.isArray()) {
                    for (JsonNode userNode : users) {
                        SupabaseUserDTO dto = mapper.treeToValue(userNode, SupabaseUserDTO.class);
                        createUser(dto);
                        log.info("Loaded user from Supabase: {} - {}", dto.getEmail(), dto.getFirstName() + " " + dto.getLastName());   
                    }
                }
            } else {
                log.error("Failed to fetch users from Supabase: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching users from Supabase", e);
        }
    }

    public SimpleUserDTO generateSimpleUserDTO(Users user) {
        SimpleUserDTO dto = new SimpleUserDTO();
        dto.setId(user.getId().toString());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProfilePicture(user.getProfilePicture());
        
        // Check if user has a public profile
        var profile = userProfileRepository.findByUserId(user.getId());
        dto.setHasPublicProfile(profile.map(p -> p.isProfilePublic()).orElse(false));
        
        return dto;
    }

    public List<SimpleUserDTO> generateSimpleUserDTO(List<Users> user) {
        return user.stream()
                .map(this::generateSimpleUserDTO)
                .collect(Collectors.toList());
    }

    public Users authUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authUserEmail = authentication.getName();
        return findByEmail(authUserEmail).orElseThrow();
    }
}
