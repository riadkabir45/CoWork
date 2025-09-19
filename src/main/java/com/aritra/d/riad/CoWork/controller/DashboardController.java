package com.aritra.d.riad.CoWork.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aritra.d.riad.CoWork.dto.CommunityUserSuggestionDTO;
import com.aritra.d.riad.CoWork.dto.DashboardUpdateDTO;
import com.aritra.d.riad.CoWork.dto.TaskSuggestionDTO;
import com.aritra.d.riad.CoWork.service.DashboardService;
import com.aritra.d.riad.CoWork.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/recent-updates")
    public ResponseEntity<?> getRecentUpdates() {
        try {
            String currentUserId = userService.getCurrentUserId();
            List<DashboardUpdateDTO> updates = dashboardService.getRecentCommunityUpdates(currentUserId);
            return ResponseEntity.ok(updates);
        } catch (Exception e) {
            log.error("Error fetching recent updates: ", e);
            return ResponseEntity.internalServerError()
                .body("Failed to fetch recent updates: " + e.getMessage());
        }
    }

    @GetMapping("/community-suggestions")
    public ResponseEntity<?> getCommunitySuggestions() {
        try {
            String currentUserId = userService.getCurrentUserId();
            List<CommunityUserSuggestionDTO> suggestions = dashboardService.getCommunitySuggestions(currentUserId);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Error fetching community suggestions: ", e);
            return ResponseEntity.internalServerError()
                .body("Failed to fetch community suggestions: " + e.getMessage());
        }
    }

    @GetMapping("/trending-tasks")
    public ResponseEntity<?> getTrendingTasks() {
        try {
            String currentUserId = userService.getCurrentUserId();
            List<TaskSuggestionDTO> suggestions = dashboardService.getTrendingTasks(currentUserId);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Error fetching trending tasks: ", e);
            return ResponseEntity.internalServerError()
                .body("Failed to fetch trending tasks: " + e.getMessage());
        }
    }
}