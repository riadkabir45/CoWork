package com.aritra.d.riad.CoWork.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardUpdateDTO {
    private String id;
    private String taskInstanceId; // Task instance ID for comments
    private String taskName;
    private String userName;
    private String updateText;
    private LocalDateTime timestamp;
    private List<TagDTO> tags;
}