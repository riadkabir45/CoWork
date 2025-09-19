package com.aritra.d.riad.CoWork.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aritra.d.riad.CoWork.model.Tag;
import com.aritra.d.riad.CoWork.model.TagSuggestion;
import com.aritra.d.riad.CoWork.model.TaskTagSuggestion;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.service.TagService;
import com.aritra.d.riad.CoWork.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    // =============== TAG ENDPOINTS ===============

    /**
     * Get all approved tags
     */
    @GetMapping("/approved")
    public ResponseEntity<List<Tag>> getAllApprovedTags() {
        log.info("Getting all approved tags");
        List<Tag> tags = tagService.getAllApprovedTags();
        log.info("Found {} approved tags", tags.size());
        return ResponseEntity.ok(tags);
    }

    /**
     * Get popular tags
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Tag>> getPopularTags(@RequestParam(defaultValue = "10") int limit) {
        List<Tag> tags = tagService.getPopularTags(limit);
        return ResponseEntity.ok(tags);
    }

    /**
     * Search tags by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<Tag>> searchTags(@RequestParam String keyword) {
        List<Tag> tags = tagService.searchTags(keyword);
        return ResponseEntity.ok(tags);
    }

    /**
     * Create a new tag (auto-approved for mentors/admins)
     */
    @PostMapping
    public ResponseEntity<?> createTag(@RequestBody Map<String, String> request) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            String name = request.get("name");
            String description = request.get("description");
            String color = request.get("color");

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tag name is required"));
            }

            Tag tag = tagService.createTag(name, description, color, user);
            return ResponseEntity.ok(Map.of(
                "message", tag.isApproved() ? "Tag created and approved" : "Tag created, pending approval",
                "tag", tag
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create tag"));
        }
    }

    // =============== TAG SUGGESTION ENDPOINTS ===============

    /**
     * Suggest a new tag
     */
    @PostMapping("/suggest")
    public ResponseEntity<?> suggestNewTag(@RequestBody Map<String, String> request) {
        try {
            System.out.println("DEBUG: Received new tag suggestion request: " + request);
            
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("DEBUG: User email from auth: " + userEmail);
            
            Users user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
            System.out.println("DEBUG: Found user: " + user.getId());

            String name = request.get("name");
            String description = request.get("description");
            String reason = request.get("reason");
            
            System.out.println("DEBUG: Tag details - name: " + name + ", description: " + description + ", reason: " + reason);

            if (name == null || name.trim().isEmpty()) {
                System.out.println("DEBUG: Tag name is null or empty");
                return ResponseEntity.badRequest().body(Map.of("error", "Tag name is required"));
            }

            if (reason == null || reason.trim().isEmpty()) {
                System.out.println("DEBUG: Reason is null or empty");
                return ResponseEntity.badRequest().body(Map.of("error", "Reason for suggestion is required"));
            }

            System.out.println("DEBUG: Calling tagService.suggestNewTag");
            TagSuggestion suggestion = tagService.suggestNewTag(name, description, reason, user);
            System.out.println("DEBUG: Tag suggestion created successfully: " + suggestion.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tag suggestion submitted for review",
                "suggestionId", suggestion.getId(),
                "suggestedName", suggestion.getSuggestedName()
            ));

        } catch (IllegalArgumentException e) {
            System.out.println("DEBUG: IllegalArgumentException: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("DEBUG: Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to submit suggestion"));
        }
    }

    /**
     * Get pending tag suggestions (mentors/admins only)
     */
    @GetMapping("/suggestions/pending")
    public ResponseEntity<?> getPendingTagSuggestions() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.hasRole("MENTOR") && !user.hasRole("ADMIN") && !user.hasRole("MODERATOR")) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            List<TagSuggestion> suggestions = tagService.getPendingSuggestions();
            return ResponseEntity.ok(suggestions);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch suggestions"));
        }
    }

    /**
     * Approve a tag suggestion (mentors/admins only)
     */
    @PostMapping("/suggestions/{suggestionId}/approve")
    public ResponseEntity<?> approveTagSuggestion(
            @PathVariable String suggestionId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            String comment = request != null ? request.get("comment") : "";
            TagSuggestion suggestion = tagService.approveSuggestion(suggestionId, comment, user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Tag suggestion approved and tag created",
                "suggestion", suggestion
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to approve suggestion"));
        }
    }

    /**
     * Reject a tag suggestion (mentors/admins only)
     */
    @PostMapping("/suggestions/{suggestionId}/reject")
    public ResponseEntity<?> rejectTagSuggestion(
            @PathVariable String suggestionId,
            @RequestBody Map<String, String> request) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            String comment = request.get("comment");
            if (comment == null || comment.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Comment is required for rejection"));
            }

            TagSuggestion suggestion = tagService.rejectSuggestion(suggestionId, comment, user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Tag suggestion rejected",
                "suggestion", suggestion
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to reject suggestion"));
        }
    }

    // =============== TASK TAG SUGGESTION ENDPOINTS ===============

    /**
     * Suggest a tag for an existing task
     */
    @PostMapping("/suggest-for-task")
    public ResponseEntity<?> suggestTagForTask(@RequestBody Map<String, String> request) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            String tagId = request.get("tagId");
            String taskId = request.get("taskId");
            String reason = request.get("reason");

            if (tagId == null || tagId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tag ID is required"));
            }

            if (taskId == null || taskId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Task ID is required"));
            }

            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reason for suggestion is required"));
            }

            TaskTagSuggestion suggestion = tagService.suggestTagForTask(tagId, taskId, reason, user);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tag suggestion for task submitted for review",
                "suggestionId", suggestion.getId()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to submit task tag suggestion"));
        }
    }

    /**
     * Get pending task tag suggestions (mentors/admins only)
     */
    @GetMapping("/task-suggestions/pending")
    public ResponseEntity<?> getPendingTaskTagSuggestions() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.hasRole("MENTOR") && !user.hasRole("ADMIN") && !user.hasRole("MODERATOR")) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            List<TaskTagSuggestion> suggestions = tagService.getPendingTaskTagSuggestions();
            return ResponseEntity.ok(suggestions);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch task tag suggestions"));
        }
    }

    /**
     * Approve a task tag suggestion (mentors/admins only)
     */
    @PostMapping("/task-suggestions/{suggestionId}/approve")
    public ResponseEntity<?> approveTaskTagSuggestion(
            @PathVariable String suggestionId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            String comment = request != null ? request.get("comment") : "";
            TaskTagSuggestion suggestion = tagService.approveTaskTagSuggestion(suggestionId, comment, user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Task tag suggestion approved",
                "suggestion", suggestion
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to approve task tag suggestion"));
        }
    }

    /**
     * Reject a task tag suggestion (mentors/admins only)
     */
    @PostMapping("/task-suggestions/{suggestionId}/reject")
    public ResponseEntity<?> rejectTaskTagSuggestion(
            @PathVariable String suggestionId,
            @RequestBody Map<String, String> request) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            String comment = request.get("comment");
            if (comment == null || comment.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Comment is required for rejection"));
            }

            TaskTagSuggestion suggestion = tagService.rejectTaskTagSuggestion(suggestionId, comment, user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Task tag suggestion rejected",
                "suggestion", suggestion
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to reject task tag suggestion"));
        }
    }

    // =============== UTILITY ENDPOINTS ===============

    /**
     * Get tags for a specific task
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<Tag>> getTagsForTask(@PathVariable String taskId) {
        List<Tag> tags = tagService.getTagsForTask(taskId);
        return ResponseEntity.ok(tags);
    }
}