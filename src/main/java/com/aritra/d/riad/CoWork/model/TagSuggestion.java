package com.aritra.d.riad.CoWork.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "tag_suggestions")
public class TagSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String suggestedName;

    @Column(length = 500)
    private String description;

    @Column(length = 1000)
    private String reason; // Why this tag is needed

    @ManyToOne
    @JoinColumn(name = "suggested_by", nullable = false)
    private Users suggestedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus status = SuggestionStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private Users reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(length = 1000)
    private String moderatorComments;

    // If approved, reference to the created tag
    @ManyToOne
    @JoinColumn(name = "created_tag_id")
    private Tag createdTag;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Approve this suggestion and create the tag
     */
    public void approve(Users reviewer, String comments, Tag createdTag) {
        this.status = SuggestionStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.moderatorComments = comments;
        this.createdTag = createdTag;
    }

    /**
     * Reject this suggestion
     */
    public void reject(Users reviewer, String comments) {
        this.status = SuggestionStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.moderatorComments = comments;
    }

    public enum SuggestionStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}