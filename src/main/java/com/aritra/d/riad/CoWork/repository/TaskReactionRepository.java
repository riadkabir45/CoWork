package com.aritra.d.riad.CoWork.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.TaskComment;
import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.model.TaskReaction;
import com.aritra.d.riad.CoWork.model.Users;

@Repository
public interface TaskReactionRepository extends JpaRepository<TaskReaction, String> {
    
    // Find user's reaction to a specific target (task instance or comment)
    Optional<TaskReaction> findByUserAndReactionTargetTypeAndTargetId(
        Users user, 
        TaskReaction.ReactionTargetType targetType, 
        String targetId
    );
    
    // Find all reactions for a task instance
    List<TaskReaction> findByTaskInstanceAndReactionTargetType(
        TaskInstances taskInstance, 
        TaskReaction.ReactionTargetType targetType
    );
    
    // Find all reactions for a comment
    List<TaskReaction> findByCommentAndReactionTargetType(
        TaskComment comment, 
        TaskReaction.ReactionTargetType targetType
    );
    
    // Count likes for a specific target
    @Query("SELECT COUNT(r) FROM TaskReaction r WHERE r.targetId = :targetId AND r.reactionTargetType = :targetType AND r.reactionType = 'LIKE'")
    long countLikesByTargetIdAndType(@Param("targetId") String targetId, @Param("targetType") TaskReaction.ReactionTargetType targetType);
    
    // Count dislikes for a specific target
    @Query("SELECT COUNT(r) FROM TaskReaction r WHERE r.targetId = :targetId AND r.reactionTargetType = :targetType AND r.reactionType = 'DISLIKE'")
    long countDislikesByTargetIdAndType(@Param("targetId") String targetId, @Param("targetType") TaskReaction.ReactionTargetType targetType);
    
    // Find all reactions by a user
    List<TaskReaction> findByUserOrderByCreatedAtDesc(Users user);
    
    // Find reactions for a task instance by reaction type
    List<TaskReaction> findByTaskInstanceAndReactionType(TaskInstances taskInstance, TaskReaction.ReactionType reactionType);
    
    // Delete user's reaction to a specific target
    void deleteByUserAndTargetIdAndReactionTargetType(Users user, String targetId, TaskReaction.ReactionTargetType targetType);
    
    // Get reaction statistics for a task instance
    @Query("SELECT r.reactionType, COUNT(r) FROM TaskReaction r WHERE r.taskInstance = :taskInstance AND r.reactionTargetType = 'TASK_INSTANCE' GROUP BY r.reactionType")
    List<Object[]> getReactionStatsByTaskInstance(@Param("taskInstance") TaskInstances taskInstance);
    
    // Get reaction statistics for a comment
    @Query("SELECT r.reactionType, COUNT(r) FROM TaskReaction r WHERE r.comment = :comment AND r.reactionTargetType = 'COMMENT' GROUP BY r.reactionType")
    List<Object[]> getReactionStatsByComment(@Param("comment") TaskComment comment);
}
