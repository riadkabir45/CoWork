package com.aritra.d.riad.CoWork.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aritra.d.riad.CoWork.dto.TaskDTO;
import com.aritra.d.riad.CoWork.model.Tag;
import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.service.TagService;
import com.aritra.d.riad.CoWork.service.TasksService;
import com.aritra.d.riad.CoWork.service.UserService;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TasksService taskService;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;


    @GetMapping
    public List<TaskDTO> listTasks() {
        return taskService.generateTaskDTOList(taskService.listTasks());
    }

    @GetMapping("/{id}")
    public TaskDTO getTaskById(@PathVariable String id) {
        Tasks task = taskService.getTaskById(id);
        if (task != null) {
            return taskService.generateTaskDTO(task);
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Map<String, Object> requestData) {
        try {
            // Extract task data
            String taskName = (String) requestData.get("taskName");
            Boolean numericalTask = (Boolean) requestData.get("numericalTask");
            @SuppressWarnings("unchecked")
            List<String> tagIds = (List<String>) requestData.get("tagIds");
            
            // Validate task name
            if (taskName == null || taskName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation Error",
                    "message", "Task name is required"
                ));
            }
            
            // Validate tags
            if (tagIds == null || tagIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation Error",
                    "message", "At least one tag is required to create a task"
                ));
            }
            
            // Create task entity
            Tasks task = new Tasks();
            task.setTaskName(taskName);
            task.setNumericalTask(numericalTask != null ? numericalTask : false);
            
            // Add tags to task
            for (String tagId : tagIds) {
                tagService.getTagById(tagId).ifPresent(tag -> {
                    if (tag.isApproved()) {
                        task.addTag(tag);
                    }
                });
            }
            
            // Validate that at least one valid tag was added
            if (task.getTags() == null || task.getTags().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation Error",
                    "message", "No valid approved tags found. Please select valid tags."
                ));
            }
            
            Tasks createdTask = taskService.createTask(task);
            return ResponseEntity.ok(createdTask);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Validation Error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error", 
                "message", "Failed to create task: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
    }

    // =============== TAG MANAGEMENT ENDPOINTS ===============

    /**
     * Add a tag to a task (task creator or mentors/admins)
     */
    @PostMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<?> addTagToTask(@PathVariable String taskId, @PathVariable String tagId) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            Tasks task = taskService.getTaskById(taskId);
            Tag tag = tagService.getTagById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

            // Check permissions: task creator, or mentor/admin
            if (!task.getCreatedBy().getId().equals(user.getId()) && 
                !user.hasRole("MENTOR") && !user.hasRole("ADMIN") && !user.hasRole("MODERATOR")) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            if (!tag.isApproved()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot add unapproved tag"));
            }

            if (task.hasTag(tag)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Task already has this tag"));
            }

            task.addTag(tag);
            taskService.createTask(task); // Save the task

            return ResponseEntity.ok(Map.of(
                "message", "Tag added to task successfully",
                "task", taskService.generateTaskDTO(task)
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to add tag to task"));
        }
    }

    /**
     * Remove a tag from a task (task creator or mentors/admins)
     */
    @DeleteMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<?> removeTagFromTask(@PathVariable String taskId, @PathVariable String tagId) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            Tasks task = taskService.getTaskById(taskId);
            Tag tag = tagService.getTagById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

            // Check permissions: task creator, or mentor/admin
            if (!task.getCreatedBy().getId().equals(user.getId()) && 
                !user.hasRole("MENTOR") && !user.hasRole("ADMIN") && !user.hasRole("MODERATOR")) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            if (!task.hasTag(tag)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Task doesn't have this tag"));
            }

            task.removeTag(tag);
            taskService.createTask(task); // Save the task

            return ResponseEntity.ok(Map.of(
                "message", "Tag removed from task successfully",
                "task", taskService.generateTaskDTO(task)
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to remove tag from task"));
        }
    }

    /**
     * Get all tags for a task
     */
    @GetMapping("/{taskId}/tags")
    public ResponseEntity<List<Tag>> getTaskTags(@PathVariable String taskId) {
        List<Tag> tags = tagService.getTagsForTask(taskId);
        return ResponseEntity.ok(tags);
    }

}
