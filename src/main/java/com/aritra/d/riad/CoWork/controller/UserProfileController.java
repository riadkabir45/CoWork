package com.aritra.d.riad.CoWork.controller;

import com.aritra.d.riad.CoWork.dto.UserProfileDTO;
import com.aritra.d.riad.CoWork.service.UserProfileService;
import com.aritra.d.riad.CoWork.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@Slf4j
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = auth != null && !auth.getPrincipal().equals("anonymousUser") 
            ? userService.findByEmail(auth.getName()).orElse(null)
            : null;

        try {
            UserProfileDTO profile = userProfileService.getUserProfile(userId, currentUser);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Profile is private")) {
                return ResponseEntity.status(403).build();
            } else if (e.getMessage().equals("User not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserProfileDTO> getUserProfileByEmail(@PathVariable String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = auth != null && !auth.getPrincipal().equals("anonymousUser") 
            ? userService.findByEmail(auth.getName()).orElse(null)
            : null;

        try {
            UserProfileDTO profile = userProfileService.getUserProfileByEmail(email, currentUser);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Profile is private")) {
                return ResponseEntity.status(403).build();
            } else if (e.getMessage().equals("User not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            @PathVariable String userId, 
            @RequestBody UserProfileDTO profileDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        try {
            UserProfileDTO updatedProfile = userProfileService.updateUserProfile(userId, profileDTO, currentUser);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Can only update own profile")) {
                return ResponseEntity.status(403).build();
            }
            throw e;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        UserProfileDTO profile = userProfileService.getUserProfile(currentUser.getId(), currentUser);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateMyProfile(@RequestBody UserProfileDTO profileDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        UserProfileDTO updatedProfile = userProfileService.updateUserProfile(currentUser.getId(), profileDTO, currentUser);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/public")
    public ResponseEntity<List<UserProfileDTO>> getPublicProfiles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = auth != null && !auth.getPrincipal().equals("anonymousUser") 
            ? userService.findByEmail(auth.getName()).orElse(null)
            : null;

        List<UserProfileDTO> profiles = userProfileService.getPublicProfiles(currentUser);
        return ResponseEntity.ok(profiles);
    }
}
