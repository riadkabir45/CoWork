package com.aritra.d.riad.CoWork.dto;

import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {
	private String id;
	private String taskName;
	private boolean isNumericalTask;
	private int taskInterval;
	private TaskIntervalType taskIntervalType;
	private Set<String> updateIds;
}
