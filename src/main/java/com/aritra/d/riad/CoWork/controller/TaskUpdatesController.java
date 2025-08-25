package com.aritra.d.riad.CoWork.controller;

import com.aritra.d.riad.CoWork.dto.TaskUpdateDTO;
import com.aritra.d.riad.CoWork.model.TaskUpdates;
import com.aritra.d.riad.CoWork.service.TaskUpdatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/task-updates")
public class TaskUpdatesController {

    @Autowired
    private TaskUpdatesService taskUpdatesService;


    @GetMapping("/")
    public List<TaskUpdateDTO> getAllTaskUpdates() {
        return taskUpdatesService.getAllTaskUpdates().stream().map(taskUpdate -> {
            TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
            taskUpdateDTO.setId(taskUpdate.getId());
            taskUpdateDTO.setUpdateTimestamp(taskUpdate.getUpdateTimestamp().toString());
            taskUpdateDTO.setUpdateDescription(taskUpdate.getUpdateDescription());
            taskUpdateDTO.setTaskInstances(taskUpdate.getTaskInstances().getId());
            return taskUpdateDTO;
        }).collect(Collectors.toList());
    }


    @GetMapping("/{id}")
    public ResponseEntity<TaskUpdates> getTaskUpdateById(@PathVariable String id) {
        TaskUpdates update = taskUpdatesService.getTaskUpdateById(id);
        return update != null ? ResponseEntity.ok(update) : ResponseEntity.notFound().build();
    }


    @PostMapping("/")
    public TaskUpdates createTaskUpdate(@RequestBody TaskUpdates taskUpdates) {
        return taskUpdatesService.createTaskUpdate(taskUpdates);
    }

    @DeleteMapping("/{id}")
    public void deleteTaskUpdate(@PathVariable String id) {
        taskUpdatesService.deleteTaskUpdate(id);
    }
}
