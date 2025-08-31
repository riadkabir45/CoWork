package com.aritra.d.riad.CoWork.model;

import com.aritra.d.riad.CoWork.enumurator.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "google_id")
    private String googleId;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "profile_picture", columnDefinition = "TEXT")
    private String profilePicture;
    
    // Many users can have many roles (user can be both MENTOR and MODERATOR)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    private String location;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "mentorship_eligible")
    private Boolean mentorshipEligible = false;
    
    @Column(name = "mentorship_rating")
    private Double mentorshipRating;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "ban_until")
    private LocalDateTime banUntil;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods for role checking
    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }
    
    public boolean hasAnyRole(String... roleNames) {
        for (String roleName : roleNames) {
            if (hasRole(roleName)) {
                return true;
            }
        }
        return false;
    }
    
    public String getPrimaryRole() {
        if (hasRole("ADMIN")) return "ADMIN";
        if (hasRole("MODERATOR")) return "MODERATOR";
        if (hasRole("MENTOR")) return "MENTOR";
        if (hasRole("REGISTERED")) return "REGISTERED";
        return "UNREGISTERED";
    }
}