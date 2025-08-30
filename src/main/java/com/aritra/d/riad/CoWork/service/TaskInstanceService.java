package com.aritra.d.riad.CoWork.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aritra.d.riad.CoWork.dto.TaskInstanceDTO;
import com.aritra.d.riad.CoWork.dto.TaskStatusDTO;
import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;
import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.repository.TaskInstancesRepository;

import jakarta.transaction.Transactional;

@Transactional
@Component
public class TaskInstanceService {

    @Autowired
    private TaskInstancesRepository taskInstancesRepository;

    public TaskInstances createTaskInstances(int taskInterval, TaskIntervalType taskIntervalType, String user, Tasks task) {
        TaskInstances taskInstance = new TaskInstances();
        taskInstance.setTaskInterval(taskInterval);
        taskInstance.setTaskIntervalType(taskIntervalType);
        taskInstance.setUserId(user);
        taskInstance.setTask(task);
        return createTaskInstance(taskInstance);
    }

    public TaskInstances createTaskInstance(TaskInstances taskInstance) {
        return taskInstancesRepository.save(taskInstance);
    }

    public TaskInstances getTaskInstanceById(String id) {
        return taskInstancesRepository.findById(id).orElseThrow(() -> new RuntimeException("Task Instance not found"));
    }

    public void deleteTaskInstance(String id) {
        TaskInstances taskInstance = getTaskInstanceById(id);
        taskInstancesRepository.delete(taskInstance);
    }

    public List<TaskInstances> listTaskInstances() {
        return taskInstancesRepository.findAll();
    }

    public List<TaskInstances> listTaskInstancesByUserId(String userId) {
        return taskInstancesRepository.findByUserId(userId);
    }

    public TaskInstanceDTO generateTaskInstanceDTO(TaskInstances taskInstance) {
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
    }

    public List<TaskInstanceDTO> generateTaskInstanceDTOList(List<TaskInstances> taskInstances) {
        return taskInstances.stream()
            .map(this::generateTaskInstanceDTO)
            .collect(Collectors.toList());
    }

    public TaskStatusDTO generateTaskStatusDTO(TaskInstances taskInstance) {
        TaskStatusDTO dto = new TaskStatusDTO();
        dto.setId(taskInstance.getId());
        dto.setName(taskInstance.getTask().getTaskName());
        if(taskInstance.getTask().isNumericalTask())
            dto.setTaskType("Number");
        else
            dto.setTaskType("Yes/No");
        dto.setInterval(taskInstance.getTaskInterval());
        dto.setIntervalType(taskInstance.getTaskIntervalType().toString());
        if(taskInstance.getTaskUpdates().size() > 0)
            dto.setLastUpdated(taskInstance.getTaskUpdates().get(taskInstance.getTaskUpdates().size() - 1).getUpdateTimestamp());
        else
            dto.setLastUpdated(null);
        dto.setUpdates(taskInstance.getTaskUpdates().stream()
            .map(update -> update.getUpdateDescription())
            .collect(Collectors.toList()));
        return dto;
    }

    public List<TaskStatusDTO> generateTaskStatusDTOList(List<TaskInstances> taskInstances) {
        return taskInstances.stream()
            .map(this::generateTaskStatusDTO)
            .collect(Collectors.toList());
    }

}
