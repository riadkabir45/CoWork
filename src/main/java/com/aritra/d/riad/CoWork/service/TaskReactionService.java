package com.aritra.d.riad.CoWork.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aritra.d.riad.CoWork.dto.TaskFeedbackStatsDTO;
import com.aritra.d.riad.CoWork.dto.TaskReactionDTO;
import com.aritra.d.riad.CoWork.model.TaskComment;
import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.model.TaskReaction;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.TaskCommentRepository;
import com.aritra.d.riad.CoWork.repository.TaskInstancesRepository;
import com.aritra.d.riad.CoWork.repository.TaskReactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class TaskReactionService {

    @Autowired
    private TaskReactionRepository taskReactionRepository;

    @Autowired
    private TaskInstancesRepository taskInstancesRepository;

    @Autowired
    private TaskCommentRepository taskCommentRepository;

    @Autowired
    private UserService userService;

    public TaskReactionDTO reactToTaskInstance(String taskInstanceId, String reactionType, Users user) {
        TaskInstances taskInstance = taskInstancesRepository.findById(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("Task instance not found"));

        TaskReaction.ReactionType reaction = TaskReaction.ReactionType.valueOf(reactionType.toUpperCase());
        
        return handleReaction(user, TaskReaction.ReactionTargetType.TASK_INSTANCE, taskInstanceId, 
                            reaction, taskInstance, null);
    }

    public TaskReactionDTO reactToComment(String commentId, String reactionType, Users user) {
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        TaskReaction.ReactionType reaction = TaskReaction.ReactionType.valueOf(reactionType.toUpperCase());
        
        return handleReaction(user, TaskReaction.ReactionTargetType.COMMENT, commentId, 
                            reaction, comment.getTaskInstance(), comment);
    }

    private TaskReactionDTO handleReaction(Users user, TaskReaction.ReactionTargetType targetType, 
                                         String targetId, TaskReaction.ReactionType newReactionType,
                                         TaskInstances taskInstance, TaskComment comment) {
        
        // Check if user has already reacted to this target
        Optional<TaskReaction> existingReaction = taskReactionRepository
                .findByUserAndReactionTargetTypeAndTargetId(user, targetType, targetId);

        TaskReaction reaction;
        
        if (existingReaction.isPresent()) {
            reaction = existingReaction.get();
            
            if (reaction.getReactionType() == newReactionType) {
                // Same reaction - remove it (toggle off)
                taskReactionRepository.delete(reaction);
                log.info("Removed {} reaction from user {} for target {}", newReactionType, user.getId(), targetId);
                return null; // Indicates reaction was removed
            } else {
                // Different reaction - update it
                reaction.setReactionType(newReactionType);
                reaction = taskReactionRepository.save(reaction);
                log.info("Updated reaction from user {} for target {} to {}", user.getId(), targetId, newReactionType);
            }
        } else {
            // New reaction
            reaction = new TaskReaction();
            reaction.setUser(user);
            reaction.setReactionType(newReactionType);
            reaction.setReactionTargetType(targetType);
            reaction.setTargetId(targetId);
            reaction.setTaskInstance(taskInstance);
            reaction.setComment(comment);
            
            reaction = taskReactionRepository.save(reaction);
            log.info("Created new {} reaction from user {} for target {}", newReactionType, user.getId(), targetId);
        }

        return convertToDTO(reaction);
    }

    public void removeReaction(String targetId, TaskReaction.ReactionTargetType targetType, Users user) {
        taskReactionRepository.deleteByUserAndTargetIdAndReactionTargetType(user, targetId, targetType);
        log.info("Removed reaction from user {} for target {}", user.getId(), targetId);
    }

    public TaskFeedbackStatsDTO getTaskInstanceFeedbackStats(String taskInstanceId, Users currentUser) {
        TaskInstances taskInstance = taskInstancesRepository.findById(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("Task instance not found"));

        TaskFeedbackStatsDTO stats = new TaskFeedbackStatsDTO();
        stats.setTaskInstanceId(taskInstanceId);

        // Comment counts
        stats.setTotalComments(taskCommentRepository.countByTaskInstance(taskInstance));
        stats.setTopLevelComments(taskCommentRepository.countByTaskInstanceAndParentCommentIsNull(taskInstance));

        // Reaction counts
        stats.setLikeCount(taskReactionRepository.countLikesByTargetIdAndType(taskInstanceId, 
                TaskReaction.ReactionTargetType.TASK_INSTANCE));
        stats.setDislikeCount(taskReactionRepository.countDislikesByTargetIdAndType(taskInstanceId, 
                TaskReaction.ReactionTargetType.TASK_INSTANCE));

        // User's reaction
        if (currentUser != null) {
            Optional<TaskReaction> userReaction = taskReactionRepository
                    .findByUserAndReactionTargetTypeAndTargetId(currentUser, 
                            TaskReaction.ReactionTargetType.TASK_INSTANCE, taskInstanceId);
            stats.setUserReaction(userReaction.map(r -> r.getReactionType().toString()).orElse(null));

            // Check if user has commented
            long userComments = taskCommentRepository.countByTaskInstanceAndParentCommentIsNull(taskInstance);
            stats.setHasUserCommented(userComments > 0);
        }

        return stats;
    }

    // New method to get feedback stats for a task (across all instances)
    public TaskFeedbackStatsDTO getTaskFeedbackStats(String taskId, Users currentUser) {
        TaskFeedbackStatsDTO stats = new TaskFeedbackStatsDTO();
        stats.setTaskInstanceId(taskId); // We'll reuse this field to store task ID
        
        // Get comment counts for the task using repository directly
        long totalComments = taskCommentRepository.countByTaskId(taskId);
        long topLevelComments = taskCommentRepository.countByTaskIdAndParentCommentIsNull(taskId);
        
        stats.setTotalComments(totalComments);
        stats.setTopLevelComments(topLevelComments);
        
        // Get aggregated reaction counts for all instances of this task
        // Note: This would require additional repository methods for task-level aggregation
        // For now, we'll set default values and implement later if needed
        stats.setLikeCount(0L);
        stats.setDislikeCount(0L);
        stats.setUserReaction(null);
        
        // Check if user has commented on any instance of this task
        if (currentUser != null) {
            // This would need a custom query to check across all task instances
            stats.setHasUserCommented(false); // Placeholder
        }
        
        return stats;
    }

    public Optional<TaskReaction> getUserReactionToTarget(String targetId, TaskReaction.ReactionTargetType targetType, 
                                                         Users user) {
        return taskReactionRepository.findByUserAndReactionTargetTypeAndTargetId(user, targetType, targetId);
    }

    private TaskReactionDTO convertToDTO(TaskReaction reaction) {
        TaskReactionDTO dto = new TaskReactionDTO();
        dto.setId(reaction.getId());
        dto.setUser(userService.generateSimpleUserDTO(reaction.getUser()));
        dto.setReactionType(reaction.getReactionType().toString());
        dto.setReactionTargetType(reaction.getReactionTargetType().toString());
        dto.setTargetId(reaction.getTargetId());
        dto.setCreatedAt(reaction.getCreatedAt());
        return dto;
    }
}
