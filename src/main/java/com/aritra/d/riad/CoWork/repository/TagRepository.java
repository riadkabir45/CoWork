package com.aritra.d.riad.CoWork.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {
    
    /**
     * Find all approved tags
     */
    List<Tag> findByApprovedTrue();
    
    /**
     * Find all unapproved tags
     */
    List<Tag> findByApprovedFalse();
    
    /**
     * Find tag by name (case insensitive)
     */
    Optional<Tag> findByNameIgnoreCase(String name);
    
    /**
     * Search approved tags by keyword in name or description
     */
    @Query("SELECT t FROM Tag t WHERE t.approved = true AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Tag> findApprovedTagsByKeyword(@Param("keyword") String keyword);
    
    /**
     * Find most popular approved tags
     */
    @Query("SELECT t FROM Tag t WHERE t.approved = true ORDER BY t.usageCount DESC")
    List<Tag> findPopularApprovedTags();
    
    /**
     * Find tags for a specific task
     */
    @Query("SELECT t FROM Tag t JOIN t.tasks task WHERE task.id = :taskId AND t.approved = true")
    List<Tag> findTagsByTaskId(@Param("taskId") String taskId);
    
    /**
     * Check if tag name already exists (case insensitive)
     */
    boolean existsByNameIgnoreCase(String name);
}