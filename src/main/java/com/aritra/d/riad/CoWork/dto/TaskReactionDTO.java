package com.aritra.d.riad.CoWork.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskReactionDTO {
    private String id;
    private SimpleUserDTO user;
    private String reactionType; // "LIKE" or "DISLIKE"
    private String reactionTargetType; // "TASK_INSTANCE" or "COMMENT"
    private String targetId; // ID of the task instance or comment
    private LocalDateTime createdAt;
}
