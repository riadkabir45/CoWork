package com.aritra.d.riad.CoWork.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskCommentDTO {
    private String id;
    private String content;
    private String taskInstanceId;
    private SimpleUserDTO author;
    private String parentCommentId;
    private List<TaskCommentDTO> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isEdited;
    private long likeCount;
    private long dislikeCount;
    private String userReaction; // "LIKE", "DISLIKE", or null
    private int replyCount;
}
