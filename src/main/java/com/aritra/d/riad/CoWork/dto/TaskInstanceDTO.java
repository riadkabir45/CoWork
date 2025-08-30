package com.aritra.d.riad.CoWork.dto;

import java.util.Set;

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
    private int taskInterval;
    private TaskIntervalType taskIntervalType;
    private String userId;
    private Set<String> taskUpdates;
}
