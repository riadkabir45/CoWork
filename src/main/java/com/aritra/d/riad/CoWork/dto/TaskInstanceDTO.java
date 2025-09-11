package com.aritra.d.riad.CoWork.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskInstanceDTO {
    private String id;
    private String task;
    private String taskId;
    private boolean isNumerical;
    private int taskInterval;
    private TaskIntervalType taskIntervalType;
    private String email;
    private List<String> taskUpdates;
    private int taskStreak;
    private LocalDateTime lastUpdated;
    private int rank;
    
    // Feedback system fields
    private long commentsCount;
    private long likeCount;
    private long dislikeCount;
    private String userReaction; // Current user's reaction: "LIKE", "DISLIKE", or null
}
