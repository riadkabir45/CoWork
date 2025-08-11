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

import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.service.TaskService;
import com.aritra.d.riad.CoWork.dto.TaskDTO;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;


    @GetMapping("/")
    public List<TaskDTO> listTasks() {
        return taskService.listTasks().stream().map(task -> {
            TaskDTO dto = new TaskDTO();
            dto.setId(task.getId());
            dto.setTaskName(task.getTaskName());
            dto.setNumericalTask(task.isNumericalTask());
            dto.setTaskInterval(task.getTaskInterval());
            dto.setTaskIntervalType(task.getTaskIntervalType());
            if (task.getUpdates() != null) {
                dto.setUpdateIds(task.getUpdates().stream().map(u -> u.getId()).collect(Collectors.toSet()));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping("/create")
    public Tasks createTask(@RequestBody Tasks task) {
        return taskService.createTask(task);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
    }

}
