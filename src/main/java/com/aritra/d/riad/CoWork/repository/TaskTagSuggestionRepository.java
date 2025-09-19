package com.aritra.d.riad.CoWork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.TaskTagSuggestion;
import com.aritra.d.riad.CoWork.model.TaskTagSuggestion.SuggestionStatus;

@Repository
public interface TaskTagSuggestionRepository extends JpaRepository<TaskTagSuggestion, String> {
    
    /**
     * Find suggestions by status
     */
    List<TaskTagSuggestion> findByStatus(SuggestionStatus status);
    
    /**
     * Find pending suggestions ordered by creation date
     */
    List<TaskTagSuggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);
    
    /**
     * Find suggestions for a specific task
     */
    @Query("SELECT tts FROM TaskTagSuggestion tts WHERE tts.task.id = :taskId ORDER BY tts.createdAt DESC")
    List<TaskTagSuggestion> findByTaskId(@Param("taskId") String taskId);
    
    /**
     * Find suggestions by user
     */
    @Query("SELECT tts FROM TaskTagSuggestion tts WHERE tts.suggestedBy.id = :userId ORDER BY tts.createdAt DESC")
    List<TaskTagSuggestion> findBySuggestedById(@Param("userId") String userId);
    
    /**
     * Check if user already suggested this tag for this task
     */
    @Query("SELECT COUNT(tts) > 0 FROM TaskTagSuggestion tts WHERE " +
           "tts.task.id = :taskId AND tts.suggestedTag.id = :tagId AND " +
           "tts.suggestedBy.id = :userId AND tts.status = 'PENDING'")
    boolean existsPendingSuggestionByTaskAndTagAndUser(
        @Param("taskId") String taskId, 
        @Param("tagId") String tagId, 
        @Param("userId") String userId
    );
    
    /**
     * Check if tag is already suggested for task (regardless of status)
     */
    @Query("SELECT COUNT(tts) > 0 FROM TaskTagSuggestion tts WHERE " +
           "tts.task.id = :taskId AND tts.suggestedTag.id = :tagId")
    boolean existsSuggestionByTaskAndTag(@Param("taskId") String taskId, @Param("tagId") String tagId);
}