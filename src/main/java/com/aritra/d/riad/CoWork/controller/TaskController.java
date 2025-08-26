package com.aritra.d.riad.CoWork.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.service.TasksService;
import com.aritra.d.riad.CoWork.dto.TaskDTO;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TasksService taskService;


    @GetMapping
    public List<TaskDTO> listTasks() {
        return taskService.generateTaskDTOList(taskService.listTasks());
    }

    @GetMapping("/{id}")
    public TaskDTO getTaskById(@PathVariable String id) {
        Tasks task = taskService.getTaskById(id);
        if (task != null) {
            return taskService.generateTaskDTO(task);
        }
        return null;
    }

    @PostMapping
    public Tasks createTask(@RequestBody Tasks task) {
        return taskService.createTask(task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
    }

}
