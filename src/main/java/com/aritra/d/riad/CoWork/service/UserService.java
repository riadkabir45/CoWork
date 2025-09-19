package com.aritra.d.riad.CoWork.service;

import com.aritra.d.riad.CoWork.dto.MentorLeaderboardDTO;
import com.aritra.d.riad.CoWork.dto.SimpleUserDTO;
import com.aritra.d.riad.CoWork.dto.SupabaseUserDTO;
import com.aritra.d.riad.CoWork.enumurator.UserStatus;
import com.aritra.d.riad.CoWork.model.Connections;
import com.aritra.d.riad.CoWork.model.Role;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.ConnectionRepository;
import com.aritra.d.riad.CoWork.repository.RoleRepository;
import com.aritra.d.riad.CoWork.repository.TaskInstancesRepository;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private ConnectionRepository connectionRepository;
    
    @Autowired
    private TaskInstancesRepository taskInstancesRepository;
    
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
    public Users promoteToMentor(Users user) {
        if (user.hasRole("MENTOR")) {
            log.info("User {} already has MENTOR role", user.getEmail());
            return user;
        }
        
        Role mentorRole = roleRepository.findByName("MENTOR").orElse(null);
        if (mentorRole == null) {
            log.error("MENTOR role not found");
            return user;
        }
        
        // Double check that the user doesn't already have this role
        if (!user.getRoles().contains(mentorRole)) {
            Set<Role> roles = new HashSet<>(user.getRoles());
            roles.add(mentorRole);
            user.setRoles(roles);
            user.setMentorshipEligible(true);
            
            return usersRepository.save(user);
        } else {
            log.info("User {} already has MENTOR role (double check)", user.getEmail());
            return user;
        }
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
        
        // Check if user already has ADMIN role
        if (user.hasRole("ADMIN")) {
            log.info("User {} already has ADMIN role", user.getEmail());
            return user;
        }
        
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        if (adminRole == null) {
            log.error("ADMIN role not found");
            return user;
        }
        
        // Double check that the user doesn't already have this role
        if (!user.getRoles().contains(adminRole)) {
            Set<Role> roles = new HashSet<>(user.getRoles());
            roles.add(adminRole);
            user.setRoles(roles);
            
            return usersRepository.save(user);
        } else {
            log.info("User {} already has ADMIN role (double check)", user.getEmail());
            return user;
        }
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
                        // Check if user already exists before creating
                        if (findByEmail(dto.getEmail()).isEmpty()) {
                            createUser(dto);
                            log.info("Loaded user from Supabase: {} - {}", dto.getEmail(), dto.getFirstName() + " " + dto.getLastName());
                        } else {
                            log.info("User already exists, skipping: {}", dto.getEmail());
                        }
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

    /**
     * Get mentor leaderboard with aggregate ratings and task information
     * @param taskFilter Optional task ID to filter mentors by specific task
     * @return List of mentors with aggregate ratings and task info
     */
    public List<MentorLeaderboardDTO> getMentorLeaderboard(String taskFilter) {
        // Get all mentors
        List<Users> mentors = usersRepository.findAll().stream()
                .filter(user -> user.hasRole("MENTOR"))
                .collect(Collectors.toList());
        
        // Filter mentors by task if specified
        if (taskFilter != null && !taskFilter.isEmpty()) {
            mentors = mentors.stream()
                    .filter(mentor -> {
                        List<String> mentorTaskIds = getUserTaskIds(mentor);
                        return mentorTaskIds.contains(taskFilter);
                    })
                    .collect(Collectors.toList());
        }
        
        Users currentUser = authUser();
        List<String> userTaskIds = getUserTaskIds(currentUser);
        
        return mentors.stream().map(mentor -> {
            MentorLeaderboardDTO dto = new MentorLeaderboardDTO();
            dto.setId(mentor.getId().toString());
            dto.setEmail(mentor.getEmail());
            dto.setFirstName(mentor.getFirstName());
            dto.setLastName(mentor.getLastName());
            dto.setProfilePicture(mentor.getProfilePicture());
            
            // Check if user has a public profile
            var profile = userProfileRepository.findByUserId(mentor.getId());
            dto.setHasPublicProfile(profile.map(p -> p.isProfilePublic()).orElse(false));
            
            // Get mentor's task information
            List<String> mentorTaskIds = getUserTaskIds(mentor);
            List<String> mentorTaskNames = getUserTaskNames(mentor);
            dto.setTaskIds(mentorTaskIds);
            dto.setTaskNames(mentorTaskNames);
            
            // Calculate aggregate ratings received by this mentor
            // Since mentors can be either sender or receiver in connections,
            // we need to check all connections where they are involved
            List<Connections> allConnections = new ArrayList<>();
            allConnections.addAll(connectionRepository.findByReceiver(mentor));
            allConnections.addAll(connectionRepository.findBySender(mentor));
            
            // Extract ratings where this mentor was rated
            // Note: Based on the UI logic, ratings are given to mentors by other users
            // So we include all non-zero ratings from connections involving this mentor
            List<Integer> ratings = allConnections.stream()
                    .map(Connections::getRateing)
                    .filter(rating -> rating != 0) // Exclude unrated connections
                    .collect(Collectors.toList());
            
            int totalRatings = ratings.size();
            int aggregateRating = ratings.stream().mapToInt(Integer::intValue).sum();
            double averageRating = totalRatings > 0 ? (double) aggregateRating / totalRatings : 0.0;
            int positiveRatings = (int) ratings.stream().filter(r -> r > 0).count();
            int negativeRatings = (int) ratings.stream().filter(r -> r < 0).count();
            
            dto.setTotalRatings(totalRatings);
            dto.setAggregateRating(aggregateRating);
            dto.setAverageRating(averageRating);
            dto.setPositiveRatings(positiveRatings);
            dto.setNegativeRatings(negativeRatings);
            
            // Determine connection status between current user and this mentor
            String connectionStatus = getConnectionStatus(currentUser, mentor);
            dto.setConnectionStatus(connectionStatus);
            
            return dto;
        })
        .sorted((a, b) -> {
            // Sort mentors with user's tasks first, then by aggregate rating
            boolean aHasUserTasks = a.getTaskIds().stream().anyMatch(userTaskIds::contains);
            boolean bHasUserTasks = b.getTaskIds().stream().anyMatch(userTaskIds::contains);
            
            if (aHasUserTasks && !bHasUserTasks) return -1;
            if (!aHasUserTasks && bHasUserTasks) return 1;
            
            // Then sort by aggregate rating (descending)
            return Integer.compare(b.getAggregateRating(), a.getAggregateRating());
        })
        .collect(Collectors.toList());
    }
    
    private List<String> getUserTaskIds(Users user) {
        return taskInstancesRepository.findByUserId(user.getId().toString()).stream()
                .map(taskInstance -> taskInstance.getTask().getId())
                .distinct()
                .collect(Collectors.toList());
    }
    
    private List<String> getUserTaskNames(Users user) {
        return taskInstancesRepository.findByUserId(user.getId().toString()).stream()
                .map(taskInstance -> taskInstance.getTask().getTaskName())
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Get connection status between two users
     * @param currentUser The authenticated user
     * @param otherUser The other user (mentor)
     * @return Connection status: "none", "pending_sent", "pending_received", "connected"
     */
    private String getConnectionStatus(Users currentUser, Users otherUser) {
        // Don't allow connection to self
        if (currentUser.getId().equals(otherUser.getId())) {
            return "self";
        }
        
        // Check if there's a connection where current user is sender
        Connections sentConnection = connectionRepository.findBySenderAndReceiver(currentUser, otherUser);
        // Check if there's a connection where current user is receiver
        Connections receivedConnection = connectionRepository.findBySenderAndReceiver(otherUser, currentUser);
        
        if (sentConnection != null) {
            return sentConnection.isAccepted() ? "connected" : "pending_sent";
        }
        
        if (receivedConnection != null) {
            return receivedConnection.isAccepted() ? "connected" : "pending_received";
        }
        
        return "none";
    }
}
