package com.aritra.d.riad.CoWork.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aritra.d.riad.CoWork.dto.TaskCommentDTO;
import com.aritra.d.riad.CoWork.dto.TaskFeedbackStatsDTO;
import com.aritra.d.riad.CoWork.dto.TaskReactionDTO;
import com.aritra.d.riad.CoWork.service.TaskCommentService;
import com.aritra.d.riad.CoWork.service.TaskReactionService;
import com.aritra.d.riad.CoWork.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/task-feedback")
@Slf4j
public class TaskFeedbackController {

    @Autowired
    private TaskCommentService taskCommentService;

    @Autowired
    private TaskReactionService taskReactionService;

    @Autowired
    private UserService userService;

    // Comment endpoints

    @PostMapping("/comments")
    public ResponseEntity<TaskCommentDTO> createComment(@RequestBody CreateCommentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        TaskCommentDTO comment = taskCommentService.createComment(
                request.getTaskInstanceId(),
                request.getContent(),
                currentUser,
                request.getParentCommentId()
        );

        return ResponseEntity.ok(comment);
    }

    @GetMapping("/comments/task-instance/{taskInstanceId}")
    public ResponseEntity<List<TaskCommentDTO>> getCommentsForTaskInstance(
            @PathVariable String taskInstanceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        if (page >= 0 && size > 0) {
            Page<TaskCommentDTO> commentPage = taskCommentService.getCommentsForTaskInstanceWithPagination(
                    taskInstanceId, currentUser, page, size);
            return ResponseEntity.ok(commentPage.getContent());
        } else {
            List<TaskCommentDTO> comments = taskCommentService.getCommentsForTaskInstance(taskInstanceId, currentUser);
            return ResponseEntity.ok(comments);
        }
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<TaskCommentDTO>> getRepliesForComment(@PathVariable String commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        List<TaskCommentDTO> replies = taskCommentService.getRepliesForComment(commentId, currentUser);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<TaskCommentDTO> getComment(@PathVariable String commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        TaskCommentDTO comment = taskCommentService.getCommentById(commentId, currentUser);
        return ResponseEntity.ok(comment);
    }

    // New endpoints for task-level comments (across all instances)
    @GetMapping("/comments/task/{taskId}")
    public ResponseEntity<List<TaskCommentDTO>> getCommentsForTask(
            @PathVariable String taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        if (page >= 0 && size > 0) {
            Page<TaskCommentDTO> commentPage = taskCommentService.getCommentsForTaskWithPagination(
                    taskId, currentUser, page, size);
            return ResponseEntity.ok(commentPage.getContent());
        } else {
            List<TaskCommentDTO> comments = taskCommentService.getCommentsForTask(taskId, currentUser);
            return ResponseEntity.ok(comments);
        }
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<TaskCommentDTO> updateComment(
            @PathVariable String commentId,
            @RequestBody UpdateCommentRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        TaskCommentDTO updatedComment = taskCommentService.updateComment(commentId, request.getContent(), currentUser);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        taskCommentService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok().build();
    }

    // Reaction endpoints

    @PostMapping("/reactions/task-instance/{taskInstanceId}")
    public ResponseEntity<Object> reactToTaskInstance(
            @PathVariable String taskInstanceId,
            @RequestBody ReactionRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        TaskReactionDTO reaction = taskReactionService.reactToTaskInstance(taskInstanceId, request.getReactionType(), currentUser);
        
        if (reaction == null) {
            // Reaction was removed (toggled off)
            return ResponseEntity.ok().body("{\"message\": \"Reaction removed\"}");
        } else {
            return ResponseEntity.ok(reaction);
        }
    }

    @PostMapping("/reactions/comment/{commentId}")
    public ResponseEntity<Object> reactToComment(
            @PathVariable String commentId,
            @RequestBody ReactionRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        TaskReactionDTO reaction = taskReactionService.reactToComment(commentId, request.getReactionType(), currentUser);
        
        if (reaction == null) {
            // Reaction was removed (toggled off)
            return ResponseEntity.ok().body("{\"message\": \"Reaction removed\"}");
        } else {
            return ResponseEntity.ok(reaction);
        }
    }

    @GetMapping("/stats/task-instance/{taskInstanceId}")
    public ResponseEntity<TaskFeedbackStatsDTO> getTaskInstanceFeedbackStats(@PathVariable String taskInstanceId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        TaskFeedbackStatsDTO stats = taskReactionService.getTaskInstanceFeedbackStats(taskInstanceId, currentUser);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/task/{taskId}")
    public ResponseEntity<TaskFeedbackStatsDTO> getTaskFeedbackStats(@PathVariable String taskId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        TaskFeedbackStatsDTO stats = taskReactionService.getTaskFeedbackStats(taskId, currentUser);
        return ResponseEntity.ok(stats);
    }

    // Request DTOs (inner classes for simplicity)
    public static class CreateCommentRequest {
        private String taskInstanceId;
        private String content;
        private String parentCommentId;

        // Getters and setters
        public String getTaskInstanceId() { return taskInstanceId; }
        public void setTaskInstanceId(String taskInstanceId) { this.taskInstanceId = taskInstanceId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getParentCommentId() { return parentCommentId; }
        public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }
    }

    public static class UpdateCommentRequest {
        private String content;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class ReactionRequest {
        private String reactionType; // "LIKE" or "DISLIKE"

        public String getReactionType() { return reactionType; }
        public void setReactionType(String reactionType) { this.reactionType = reactionType; }
    }
}
