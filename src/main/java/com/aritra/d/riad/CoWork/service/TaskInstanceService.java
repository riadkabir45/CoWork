package com.aritra.d.riad.CoWork.service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aritra.d.riad.CoWork.dto.TaskInstanceDTO;
import com.aritra.d.riad.CoWork.dto.TaskStatusDTO;
import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;
import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.model.TaskUpdates;
import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.TaskInstancesRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Transactional
@Service
@Slf4j
public class TaskInstanceService {

    @Autowired
    private TaskInstancesRepository taskInstancesRepository;

    public TaskInstances createTaskInstances(int taskInterval, TaskIntervalType taskIntervalType, Users user, Tasks task) {
        TaskInstances taskInstance = new TaskInstances();
        taskInstance.setTaskInterval(taskInterval);
        taskInstance.setTaskIntervalType(taskIntervalType);
        taskInstance.setUser(user);
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

    public List<TaskInstances> listTaskInstancesByUserEmail(String email) {
        return taskInstancesRepository.findByUserEmail(email);
    }

    public TaskInstanceDTO generateTaskInstanceDTO(TaskInstances taskInstance) {
    TaskInstanceDTO dto = new TaskInstanceDTO();
        dto.setId(taskInstance.getId());
        dto.setId(taskInstance.getTask().getId());
        dto.setTaskInterval(taskInstance.getTaskInterval());
        dto.setTaskIntervalType(taskInstance.getTaskIntervalType());
        dto.setUserId(taskInstance.getUser().getId());
        dto.setTaskUpdates(taskInstance.getTaskUpdates().stream()
            .map(update -> update.getId())
            .collect(Collectors.toSet()));
        dto.setTaskStreak(taskInstance.getTaskStreak());
        return dto;
    }

    public List<TaskInstanceDTO> generateTaskInstanceDTOList(List<TaskInstances> taskInstances) {
        return taskInstances.stream()
            .map(this::generateTaskInstanceDTO)
            .collect(Collectors.toList());
    }

    public TaskStatusDTO generateTaskStatusDTO(TaskInstances taskInstance) {
        int streak = calculateTaskStreak(taskInstance);
        TaskStatusDTO dto = new TaskStatusDTO();
        dto.setId(taskInstance.getId());
        dto.setName(taskInstance.getTask().getTaskName());
        if(taskInstance.getTask().isNumericalTask())
            dto.setTaskType("Number");
        else
            dto.setTaskType("Yes/No");
        dto.setInterval(taskInstance.getTaskInterval());
        dto.setTaskStreak(streak);
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

    public int calculateTaskStreak(TaskInstances taskInstance) {
        List<TaskUpdates> updates = taskInstance.getTaskUpdates();
        if(updates == null || updates.isEmpty()) {
            log.info("No updates found for task instance: " + taskInstance.getId());
            taskInstance.setTaskStreak(0);
            return 0;
        }
        updates = updates.reversed();
        int streak = 0;
        Long interval;
        switch (taskInstance.getTaskIntervalType()) {
            case TaskIntervalType.DAYS:
                for (int i = 0; i < updates.size() - 1; i++) {
                    TaskUpdates start = updates.get(i);
                    TaskUpdates end = updates.get(i + 1);
                    interval = ChronoUnit.DAYS.between(end.getUpdateTimestamp(), start.getUpdateTimestamp());
                    log.info("Interval between " + start.getUpdateTimestamp() + " and " + end.getUpdateTimestamp() + " is " + interval + " days.");
                    if (interval <= taskInstance.getTaskInterval()) {
                        streak++;
                    }
                    else {
                        taskInstance.setTaskStreak(streak);
                    }
                }
                break;
            case TaskIntervalType.HOURS:
                for (int i = 0; i < updates.size() - 1; i++) {
                    TaskUpdates start = updates.get(i);
                    TaskUpdates end = updates.get(i + 1);
                    interval = ChronoUnit.HOURS.between(end.getUpdateTimestamp(), start.getUpdateTimestamp());
                    log.info("Interval between " + start.getUpdateTimestamp() + " and " + end.getUpdateTimestamp() + " is " + interval + " hours.");
                    if (interval <= taskInstance.getTaskInterval()) {
                        streak++;
                    }
                    else {
                        taskInstance.setTaskStreak(streak);
                    }
                }
                break;
            case TaskIntervalType.MONTHS:
                for (int i = 0; i < updates.size() - 1; i++) {
                    TaskUpdates start = updates.get(i);
                    TaskUpdates end = updates.get(i + 1);
                    interval = ChronoUnit.MONTHS.between(end.getUpdateTimestamp(), start.getUpdateTimestamp());
                    log.info("Interval between " + start.getUpdateTimestamp() + " and " + end.getUpdateTimestamp() + " is " + interval + " months.");
                    if (interval <= taskInstance.getTaskInterval()) {
                        streak++;
                    }
                    else {
                        taskInstance.setTaskStreak(streak);
                    }
                }
                break;
            case TaskIntervalType.YEARS:
                for (int i = 0; i < updates.size() - 1; i++) {
                    TaskUpdates start = updates.get(i);
                    TaskUpdates end = updates.get(i + 1);
                    interval = ChronoUnit.YEARS.between(end.getUpdateTimestamp(), start.getUpdateTimestamp());
                    log.info("Interval between " + start.getUpdateTimestamp() + " and " + end.getUpdateTimestamp() + " is " + interval + " years.");
                    if (interval <= taskInstance.getTaskInterval()) {
                        streak++;
                    }
                    else {
                        taskInstance.setTaskStreak(streak);
                    }
                }
                break;
            default:
                break;
        }
        log.info("Calculated task streak for task instance " + taskInstance.getId() + ": " + streak);
        return streak;
    }
}
