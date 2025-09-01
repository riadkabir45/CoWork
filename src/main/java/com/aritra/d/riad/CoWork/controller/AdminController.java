package com.aritra.d.riad.CoWork.controller;

import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.UsersRepository;
import com.aritra.d.riad.CoWork.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {
    
    private final UserService userService;
    private final UsersRepository usersRepository;
    
    /**
     * Get all users (admin and moderator only)
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<Users> users = usersRepository.findAll();
            
            List<Map<String, Object>> userList = users.stream()
                .map(user -> Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                    "lastName", user.getLastName() != null ? user.getLastName() : "",
                    "primaryRole", user.getPrimaryRole(),
                    "roles", user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toList()),
                    "mentorshipEligible", user.getMentorshipEligible() != null ? user.getMentorshipEligible() : false,
                    "status", user.getStatus()
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get all users (no authorization - for initial setup and testing)
     */
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsersPublic() {
        try {
            List<Users> users = usersRepository.findAll();
            
            List<Map<String, Object>> userList = users.stream()
                .map(user -> Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                    "lastName", user.getLastName() != null ? user.getLastName() : "",
                    "primaryRole", user.getPrimaryRole(),
                    "roles", user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toList()),
                    "mentorshipEligible", user.getMentorshipEligible() != null ? user.getMentorshipEligible() : false,
                    "status", user.getStatus()
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Promote user to mentor (accessible by admins and moderators)
     */
    @PostMapping("/promote-mentor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<?> promoteToMentor(@RequestBody Map<String, String> request) {
        try {
            UUID userId = UUID.fromString(request.get("userId"));
            Users updatedUser = userService.promoteToMentor(userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "User promoted to mentor successfully",
                "user", Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "primaryRole", updatedUser.getPrimaryRole(),
                    "mentorshipEligible", updatedUser.getMentorshipEligible()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Assign moderator role to user (admin only)
     */
    @PostMapping("/assign-moderator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignModerator(@RequestBody Map<String, String> request) {
        try {
            UUID userId = UUID.fromString(request.get("userId"));
            Users updatedUser = userService.assignModerator(userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "User assigned moderator role successfully",
                "user", Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "primaryRole", updatedUser.getPrimaryRole()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Remove moderator role from user (admin only)
     */
    @DeleteMapping("/remove-moderator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeModerator(@RequestBody Map<String, String> request) {
        try {
            UUID userId = UUID.fromString(request.get("userId"));
            Users updatedUser = userService.removeModerator(userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Moderator role removed successfully",
                "user", Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "primaryRole", updatedUser.getPrimaryRole()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Temporary endpoint for initial admin setup (remove after testing)
     * This endpoint has no authorization so you can assign the first admin
     */
    @PostMapping("/setup-admin")
    public ResponseEntity<?> setupAdmin(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            // Find user by email
            Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            
            // Assign admin role
            Users updatedUser = userService.assignAdmin(user.getId());
            
            return ResponseEntity.ok(Map.of(
                "message", "Admin role assigned successfully! User can now access admin features.",
                "user", Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "primaryRole", updatedUser.getPrimaryRole(),
                    "roles", updatedUser.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toList())
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Assign any role to user by email (for testing - remove in production)
     */
    @PostMapping("/assign-role")
    public ResponseEntity<?> assignRole(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String roleName = request.get("roleName");
            
            // Find user by email
            Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            
            // Assign role
            Users updatedUser = userService.assignRole(user.getId(), roleName);
            
            return ResponseEntity.ok(Map.of(
                "message", "Role assigned successfully!",
                "user", Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "primaryRole", updatedUser.getPrimaryRole(),
                    "roles", updatedUser.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toList())
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
