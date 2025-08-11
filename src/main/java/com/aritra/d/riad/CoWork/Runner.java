package com.aritra.d.riad.CoWork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;
import com.aritra.d.riad.CoWork.model.TaskUpdates;
import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.service.TaskService;
import com.aritra.d.riad.CoWork.service.TaskUpdatesService;

@Component
public class Runner implements CommandLineRunner {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskUpdatesService taskUpdatesService;

    @Override
    public void run(String... args) {
        Tasks task = taskService.createTask("Sample Task", true, 1, TaskIntervalType.DAYS);
        TaskUpdates taskUpdates = new TaskUpdates();
        taskUpdates.setUpdatedBy("User1");
        taskUpdates.setTask(task);
        taskUpdates.setUpdateDescription("Test creation");
        taskUpdatesService.createTaskUpdate(taskUpdates);
    }
}