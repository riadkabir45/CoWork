package com.aritra.d.riad.CoWork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.TagSuggestion;
import com.aritra.d.riad.CoWork.model.TagSuggestion.SuggestionStatus;

@Repository
public interface TagSuggestionRepository extends JpaRepository<TagSuggestion, String> {
    
    /**
     * Find suggestions by status
     */
    List<TagSuggestion> findByStatus(SuggestionStatus status);
    
    /**
     * Find pending suggestions
     */
    List<TagSuggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);
    
    /**
     * Find suggestions by user
     */
    @Query("SELECT ts FROM TagSuggestion ts WHERE ts.suggestedBy.id = :userId ORDER BY ts.createdAt DESC")
    List<TagSuggestion> findBySuggestedById(@Param("userId") String userId);
    
    /**
     * Check if user already suggested this tag name
     */
    @Query("SELECT COUNT(ts) > 0 FROM TagSuggestion ts WHERE " +
           "ts.suggestedBy.id = :userId AND LOWER(ts.suggestedName) = LOWER(:tagName) AND ts.status = 'PENDING'")
    boolean existsPendingSuggestionByUserAndName(@Param("userId") String userId, @Param("tagName") String tagName);
}