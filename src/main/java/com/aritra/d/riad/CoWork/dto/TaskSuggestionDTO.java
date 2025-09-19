package com.aritra.d.riad.CoWork.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSuggestionDTO {
    private String id;
    private String taskName;
    private String description;
    private List<TagDTO> tags;
    private int userCount;
    private double completionRate;
}