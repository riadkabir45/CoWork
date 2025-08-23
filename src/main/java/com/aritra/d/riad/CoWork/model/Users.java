package com.aritra.d.riad.CoWork.model;

import com.aritra.d.riad.CoWork.enumurator.UserRole;
import com.aritra.d.riad.CoWork.enumurator.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.UNREGISTERED;
    
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
}