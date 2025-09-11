package com.aritra.d.riad.CoWork.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskFeedbackStatsDTO {
    private String taskInstanceId;
    private long totalComments;
    private long topLevelComments;
    private long likeCount;
    private long dislikeCount;
    private String userReaction; // Current user's reaction: "LIKE", "DISLIKE", or null
    private boolean hasUserCommented;
}
