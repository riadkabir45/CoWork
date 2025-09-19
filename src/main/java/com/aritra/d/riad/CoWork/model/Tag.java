package com.aritra.d.riad.CoWork.model;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "tags")
@EqualsAndHashCode(exclude = {"tasks"})
@ToString(exclude = {"tasks"})
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean approved = false;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private Users creator;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

    // Color for UI display (hex color code)
    @Column(name = "color", length = 7)
    private String color = "#3B82F6"; // Default blue

    // Usage count for popular tags
    @Column(name = "usage_count")
    private Integer usageCount = 0;

    // Tasks that use this tag
    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    private Set<Tasks> tasks;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Approve this tag
     */
    public void approve(Users approver) {
        this.approved = true;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * Increment usage count when tag is used
     */
    public void incrementUsage() {
        this.usageCount++;
    }

    /**
     * Check if tag is approved
     */
    public boolean isApproved() {
        return this.approved;
    }
}