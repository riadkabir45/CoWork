package com.aritra.d.riad.CoWork.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskUpdateDTO {
    private String id;
    private String updatedBy;
    private String updateTimestamp;
    private String updateDescription;
    private String taskId;
}
