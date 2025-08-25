package com.aritra.d.riad.CoWork.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aritra.d.riad.CoWork.dto.TaskInstanceDTO;
import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.service.TaskInstanceService;

@RestController
@RequestMapping("/task-instances")
public class TaskInstancesController {
    
    @Autowired
    private TaskInstanceService taskInstanceService;

    @GetMapping
    public List<TaskInstanceDTO> getAllTaskInstances() {
        return taskInstanceService.listTaskInstances().stream().map(taskInstance -> {
            TaskInstanceDTO dto = new TaskInstanceDTO();
            dto.setId(taskInstance.getId());
            dto.setId(taskInstance.getTask().getId());
            dto.setTaskInterval(taskInstance.getTaskInterval());
            dto.setTaskIntervalType(taskInstance.getTaskIntervalType());
            dto.setUserId(taskInstance.getUserId());
            dto.setTaskUpdates(taskInstance.getTaskUpdates().stream()
                .map(update -> update.getId())
                .collect(Collectors.toSet()));
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TaskInstanceDTO getTaskInstanceById(@PathVariable String id) {
        TaskInstances taskInstance = taskInstanceService.getTaskInstanceById(id);
        if (taskInstance != null) {
            TaskInstanceDTO dto = new TaskInstanceDTO();
            dto.setId(taskInstance.getId());
            dto.setId(taskInstance.getId());
            dto.setTaskInterval(taskInstance.getTaskInterval());
            dto.setTaskIntervalType(taskInstance.getTaskIntervalType());
            dto.setUserId(taskInstance.getUserId());
            dto.setTaskUpdates(taskInstance.getTaskUpdates().stream()
                .map(update -> update.getId())
                .collect(Collectors.toSet()));
            return dto;
        }
        return null;
    }

    @PostMapping
    public TaskInstances createTaskInstance(@RequestBody TaskInstances taskInstance) {
        return taskInstanceService.createTaskInstance(taskInstance);
    }

    @DeleteMapping("/{id}")
    public void deleteTaskInstance(@PathVariable String id) {
        taskInstanceService.deleteTaskInstance(id);
    }
}
