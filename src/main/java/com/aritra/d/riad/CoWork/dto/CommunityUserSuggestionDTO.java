package com.aritra.d.riad.CoWork.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityUserSuggestionDTO {
    private String id;
    private String name;
    private String email;
    private String profilePicture;
    private int commonTasks;
    private int mutualConnections;
    private int activeStreak;
}