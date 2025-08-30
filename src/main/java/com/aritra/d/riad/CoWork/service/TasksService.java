package com.aritra.d.riad.CoWork.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aritra.d.riad.CoWork.dto.TaskDTO;
import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.repository.TasksRepository;

import jakarta.transaction.Transactional;

@Transactional
@Component
public class TasksService {

    @Autowired
    TasksRepository tasksRepository;

    public Tasks createTask(String taskName, boolean isNumericalTask) {
        Tasks task = new Tasks();
        task.setTaskName(taskName);
        task.setNumericalTask(isNumericalTask);
        return createTask(task);
    }

    public Tasks createTask(Tasks task) {
        tasksRepository.save(task);
        return task;
    }

    public Tasks getTaskById(String id) {
        Optional<Tasks> task = tasksRepository.findById(id);
        return task.orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public void deleteTask(String id) {
        Tasks task = getTaskById(id);
        tasksRepository.delete(task);
    }

    public List<Tasks> listTasks() {
        return tasksRepository.findAll();
    }

    public TaskDTO generateTaskDTO(Tasks task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTaskName(task.getTaskName());
        dto.setNumericalTask(task.isNumericalTask());
        if (task.getInstances() != null) {
            dto.setInstances(task.getInstances().stream().map(u -> u.getId()).collect(Collectors.toSet()));
        }
        return dto;
    }

    public List<TaskDTO> generateTaskDTOList(List<Tasks> tasks) {
        return tasks.stream()
            .map(this::generateTaskDTO)
            .collect(Collectors.toList());
    }

}
