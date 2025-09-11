package com.aritra.d.riad.CoWork.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private String bio;
    private String location;
    private String website;
    private String linkedinUrl;
    private String githubUrl;
    private String twitterUrl;
    private String company;
    private String jobTitle;
    private List<String> skills;
    private List<String> interests;
    private LocalDateTime joinedDate;
    
    // Stats (when visible)
    private Long totalTasks;
    private Long completedTasks;
    private Integer currentStreak;
    private Long totalComments;
    
    // Privacy flags (only for profile owner)
    private Boolean showEmail;
    private Boolean showLocation;
    private Boolean showCompany;
    private Boolean showSocialLinks;
    private Boolean showSkills;
    private Boolean showInterests;
    private Boolean showTaskStats;
    private Boolean isProfilePublic;
    
    // Public view flags (for visitors)
    private Boolean isOwnProfile;
    private Boolean canViewProfile;
}
