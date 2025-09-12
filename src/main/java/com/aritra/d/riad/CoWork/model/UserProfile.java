package com.aritra.d.riad.CoWork.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    
    @Id
    private String id;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Users user;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "website")
    private String website;
    
    @Column(name = "linkedin_url")
    private String linkedinUrl;
    
    @Column(name = "github_url")
    private String githubUrl;
    
    @Column(name = "twitter_url")
    private String twitterUrl;
    
    @Column(name = "company")
    private String company;
    
    @Column(name = "job_title")
    private String jobTitle;
    
    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills; // JSON string or comma-separated values
    
    @Column(name = "interests", columnDefinition = "TEXT")
    private String interests; // JSON string or comma-separated values
    
    // Privacy settings
    @Column(name = "show_email", nullable = false)
    private boolean showEmail = false;
    
    @Column(name = "show_location", nullable = false)
    private boolean showLocation = true;
    
    @Column(name = "show_company", nullable = false)
    private boolean showCompany = true;
    
    @Column(name = "show_social_links", nullable = false)
    private boolean showSocialLinks = true;
    
    @Column(name = "show_skills", nullable = false)
    private boolean showSkills = true;
    
    @Column(name = "show_interests", nullable = false)
    private boolean showInterests = true;
    
    @Column(name = "show_task_stats", nullable = false)
    private boolean showTaskStats = true;
    
    @Column(name = "is_profile_public", nullable = false)
    private boolean isProfilePublic = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
