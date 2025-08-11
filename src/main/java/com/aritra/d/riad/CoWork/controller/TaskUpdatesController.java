package com.aritra.d.riad.CoWork.controller;

import com.aritra.d.riad.CoWork.model.TaskUpdates;
import com.aritra.d.riad.CoWork.service.TaskUpdatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task-updates")
public class TaskUpdatesController {

    @Autowired
    private TaskUpdatesService taskUpdatesService;


    @GetMapping("/")
    public List<TaskUpdates> getAllTaskUpdates() {
        return taskUpdatesService.getAllTaskUpdates();
    }


    @GetMapping("/{id}")
    public ResponseEntity<TaskUpdates> getTaskUpdateById(@PathVariable String id) {
        TaskUpdates update = taskUpdatesService.getTaskUpdateById(id);
        return update != null ? ResponseEntity.ok(update) : ResponseEntity.notFound().build();
    }


    @PostMapping
    public TaskUpdates createTaskUpdate(@RequestBody TaskUpdates taskUpdates) {
        return taskUpdatesService.createTaskUpdate(taskUpdates);
    }

    @DeleteMapping("/{id}")
    public void deleteTaskUpdate(@PathVariable String id) {
        taskUpdatesService.deleteTaskUpdate(id);
    }
}
