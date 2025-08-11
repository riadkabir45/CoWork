package com.aritra.d.riad.CoWork.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aritra.d.riad.CoWork.model.TaskUpdates;
import com.aritra.d.riad.CoWork.repository.TaskUpdatesRepository;

@Component
@Transactional
public class TaskUpdatesService {

    @Autowired
    private TaskUpdatesRepository taskUpdatesRepository;

    public List<TaskUpdates> getAllTaskUpdates() {
        return taskUpdatesRepository.findAll();
    }

    public TaskUpdates getTaskUpdateById(String id) {
        return taskUpdatesRepository.findById(id).orElseThrow(() -> new RuntimeException("TaskUpdate not found"));
    }

    public TaskUpdates createTaskUpdate(TaskUpdates taskUpdate) {
        return taskUpdatesRepository.save(taskUpdate);
    }

    public void deleteTaskUpdate(String id) {
        TaskUpdates taskUpdates = getTaskUpdateById(id);
        taskUpdatesRepository.delete(taskUpdates);
    }
}
