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
public class TaskStatusDTO {
    private String id;
    private String name;
    private String taskType;
    private int interval;
    private String intervalType;
    private LocalDateTime lastUpdated;
    private List<String> updates;
}
