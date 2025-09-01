package com.aritra.d.riad.CoWork.dto;

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
	private String type;
	private Set<String> instances;
}
