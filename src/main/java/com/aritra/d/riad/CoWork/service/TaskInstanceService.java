package com.aritra.d.riad.CoWork.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

}
