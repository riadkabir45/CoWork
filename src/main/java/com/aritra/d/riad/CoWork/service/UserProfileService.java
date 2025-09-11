package com.aritra.d.riad.CoWork.service;

import com.aritra.d.riad.CoWork.dto.UserProfileDTO;
import com.aritra.d.riad.CoWork.model.UserProfile;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.UserProfileRepository;
import com.aritra.d.riad.CoWork.repository.TaskInstancesRepository;
import com.aritra.d.riad.CoWork.repository.TaskCommentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private TaskInstancesRepository taskInstancesRepository;
    
    @Autowired
    private TaskCommentRepository taskCommentRepository;
    
    @Autowired
    private UserService userService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserProfileDTO getUserProfile(String userId, Users currentUser) {
        Users targetUser = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(createDefaultProfile(targetUser));
        
        boolean isOwnProfile = currentUser != null && currentUser.getId().equals(userId);
        boolean canViewProfile = isOwnProfile || profile.isProfilePublic();
        
        if (!canViewProfile) {
            throw new RuntimeException("Profile is private");
        }
        
        return convertToDTO(profile, isOwnProfile);
    }
    
    public UserProfileDTO getUserProfileByEmail(String email, Users currentUser) {
        Users targetUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return getUserProfile(targetUser.getId(), currentUser);
    }

    public UserProfileDTO updateUserProfile(String userId, UserProfileDTO profileDTO, Users currentUser) {
        if (!currentUser.getId().equals(userId)) {
            throw new RuntimeException("Can only update own profile");
        }
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(createDefaultProfile(currentUser));
        
        updateProfileFromDTO(profile, profileDTO);
        UserProfile savedProfile = userProfileRepository.save(profile);
        
        return convertToDTO(savedProfile, true);
    }

    public List<UserProfileDTO> getPublicProfiles(Users currentUser) {
        List<UserProfile> publicProfiles = userProfileRepository.findAllPublicProfiles();
        return publicProfiles.stream()
                .map(profile -> convertToDTO(profile, false))
                .toList();
    }

    private UserProfile createDefaultProfile(Users user) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setId(user.getId());
        // Default privacy settings are already set in entity
        return profile;
    }

    private UserProfileDTO convertToDTO(UserProfile profile, boolean isOwnProfile) {
        UserProfileDTO dto = new UserProfileDTO();
        Users user = profile.getUser();
        
        // Basic info (always visible)
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setJoinedDate(user.getCreatedAt());
        dto.setIsOwnProfile(isOwnProfile);
        dto.setCanViewProfile(true);
        
        // Bio (always visible if public profile)
        dto.setBio(profile.getBio());
        
        // Conditional fields based on privacy settings
        if (isOwnProfile || profile.isShowEmail()) {
            dto.setEmail(user.getEmail());
        }
        
        if (isOwnProfile || profile.isShowLocation()) {
            dto.setLocation(profile.getLocation());
        }
        
        if (isOwnProfile || profile.isShowCompany()) {
            dto.setCompany(profile.getCompany());
            dto.setJobTitle(profile.getJobTitle());
        }
        
        if (isOwnProfile || profile.isShowSocialLinks()) {
            dto.setWebsite(profile.getWebsite());
            dto.setLinkedinUrl(profile.getLinkedinUrl());
            dto.setGithubUrl(profile.getGithubUrl());
            dto.setTwitterUrl(profile.getTwitterUrl());
        }
        
        if (isOwnProfile || profile.isShowSkills()) {
            dto.setSkills(parseJsonList(profile.getSkills()));
        }
        
        if (isOwnProfile || profile.isShowInterests()) {
            dto.setInterests(parseJsonList(profile.getInterests()));
        }
        
        if (isOwnProfile || profile.isShowTaskStats()) {
            // Calculate task stats
            dto.setTotalTasks(taskInstancesRepository.countByUserId(user.getId()));
            dto.setCompletedTasks(taskInstancesRepository.countCompletedTasksByUserId(user.getId()));
            dto.setCurrentStreak(calculateCurrentStreak(user.getId()));
            dto.setTotalComments(taskCommentRepository.countByAuthorId(user.getId()));
        }
        
        // Privacy settings (only for profile owner)
        if (isOwnProfile) {
            dto.setShowEmail(profile.isShowEmail());
            dto.setShowLocation(profile.isShowLocation());
            dto.setShowCompany(profile.isShowCompany());
            dto.setShowSocialLinks(profile.isShowSocialLinks());
            dto.setShowSkills(profile.isShowSkills());
            dto.setShowInterests(profile.isShowInterests());
            dto.setShowTaskStats(profile.isShowTaskStats());
            dto.setIsProfilePublic(profile.isProfilePublic());
        }
        
        return dto;
    }

    private void updateProfileFromDTO(UserProfile profile, UserProfileDTO dto) {
        if (dto.getBio() != null) {
            profile.setBio(dto.getBio());
        }
        if (dto.getLocation() != null) {
            profile.setLocation(dto.getLocation());
        }
        if (dto.getWebsite() != null) {
            profile.setWebsite(dto.getWebsite());
        }
        if (dto.getLinkedinUrl() != null) {
            profile.setLinkedinUrl(dto.getLinkedinUrl());
        }
        if (dto.getGithubUrl() != null) {
            profile.setGithubUrl(dto.getGithubUrl());
        }
        if (dto.getTwitterUrl() != null) {
            profile.setTwitterUrl(dto.getTwitterUrl());
        }
        if (dto.getCompany() != null) {
            profile.setCompany(dto.getCompany());
        }
        if (dto.getJobTitle() != null) {
            profile.setJobTitle(dto.getJobTitle());
        }
        if (dto.getSkills() != null) {
            profile.setSkills(toJsonString(dto.getSkills()));
        }
        if (dto.getInterests() != null) {
            profile.setInterests(toJsonString(dto.getInterests()));
        }
        
        // Privacy settings
        if (dto.getShowEmail() != null) {
            profile.setShowEmail(dto.getShowEmail());
        }
        if (dto.getShowLocation() != null) {
            profile.setShowLocation(dto.getShowLocation());
        }
        if (dto.getShowCompany() != null) {
            profile.setShowCompany(dto.getShowCompany());
        }
        if (dto.getShowSocialLinks() != null) {
            profile.setShowSocialLinks(dto.getShowSocialLinks());
        }
        if (dto.getShowSkills() != null) {
            profile.setShowSkills(dto.getShowSkills());
        }
        if (dto.getShowInterests() != null) {
            profile.setShowInterests(dto.getShowInterests());
        }
        if (dto.getShowTaskStats() != null) {
            profile.setShowTaskStats(dto.getShowTaskStats());
        }
        if (dto.getIsProfilePublic() != null) {
            profile.setProfilePublic(dto.getIsProfilePublic());
        }
    }

    private List<String> parseJsonList(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON list: {}", jsonString, e);
            return new ArrayList<>();
        }
    }

    private String toJsonString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("Failed to convert list to JSON: {}", list, e);
            return null;
        }
    }

    private Integer calculateCurrentStreak(String userId) {
        // This is a simplified version - you might want to implement more complex logic
        return taskInstancesRepository.getCurrentStreakForUser(userId);
    }
}
