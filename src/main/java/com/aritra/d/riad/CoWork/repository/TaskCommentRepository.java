package com.aritra.d.riad.CoWork.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.TaskComment;
import com.aritra.d.riad.CoWork.model.TaskInstances;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, String> {
    
    // Find all top-level comments for a task instance (ordered by creation date)
    List<TaskComment> findByTaskInstanceAndParentCommentIsNullOrderByCreatedAtDesc(TaskInstances taskInstance);
    
    // Find all top-level comments for a task instance with pagination
    Page<TaskComment> findByTaskInstanceAndParentCommentIsNullOrderByCreatedAtDesc(TaskInstances taskInstance, Pageable pageable);
    
    // Find replies to a specific comment
    List<TaskComment> findByParentCommentOrderByCreatedAtAsc(TaskComment parentComment);
    
    // Find all comments by a user for a specific task instance
    List<TaskComment> findByTaskInstanceAndAuthorOrderByCreatedAtDesc(TaskInstances taskInstance, com.aritra.d.riad.CoWork.model.Users author);
    
    // Count total comments for a task instance (including replies)
    long countByTaskInstance(TaskInstances taskInstance);
    
    // Count top-level comments for a task instance
    long countByTaskInstanceAndParentCommentIsNull(TaskInstances taskInstance);
    
    // Find recent comments across all task instances (for activity feed)
    @Query("SELECT c FROM TaskComment c WHERE c.parentComment IS NULL ORDER BY c.createdAt DESC")
    Page<TaskComment> findRecentTopLevelComments(Pageable pageable);
    
    // Find comments by user across all task instances
    List<TaskComment> findByAuthorOrderByCreatedAtDesc(com.aritra.d.riad.CoWork.model.Users author);
    
    // Search comments by content
    @Query("SELECT c FROM TaskComment c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY c.createdAt DESC")
    Page<TaskComment> searchByContent(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Count comments by author
    @Query("SELECT COUNT(c) FROM TaskComment c WHERE c.author.id = :authorId")
    Long countByAuthorId(@Param("authorId") String authorId);
}
