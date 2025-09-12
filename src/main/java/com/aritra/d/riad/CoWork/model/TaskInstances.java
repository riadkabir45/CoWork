package com.aritra.d.riad.CoWork.model;

import java.util.ArrayList;
import java.util.List;

import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskInstances {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private int taskInterval;
    private TaskIntervalType taskIntervalType;
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;
    private int taskStreak = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tasks task;

    @OneToMany(mappedBy = "taskInstances",cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaskUpdates> taskUpdates = new ArrayList<>();

    @OneToMany(mappedBy = "taskInstance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaskComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "taskInstance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaskReaction> reactions = new ArrayList<>();

    // Helper methods for reactions
    public long getLikeCount() {
        return reactions.stream()
                .filter(reaction -> reaction.getReactionType() == TaskReaction.ReactionType.LIKE 
                        && reaction.getReactionTargetType() == TaskReaction.ReactionTargetType.TASK_INSTANCE)
                .count();
    }

    public long getDislikeCount() {
        return reactions.stream()
                .filter(reaction -> reaction.getReactionType() == TaskReaction.ReactionType.DISLIKE 
                        && reaction.getReactionTargetType() == TaskReaction.ReactionTargetType.TASK_INSTANCE)
                .count();
    }

    public long getCommentsCount() {
        return comments.stream()
                .filter(comment -> comment.getParentComment() == null) // Only count top-level comments
                .count();
    }
}
