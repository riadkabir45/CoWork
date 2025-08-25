package com.aritra.d.riad.CoWork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;
import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.model.TaskUpdates;
import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.service.TaskInstanceService;
import com.aritra.d.riad.CoWork.service.TasksService;
import com.aritra.d.riad.CoWork.service.TaskUpdatesService;

@Component
public class Runner implements CommandLineRunner {

    @Autowired
    private TasksService taskService;

    @Autowired
    private TaskUpdatesService taskUpdatesService;

    @Autowired
    private TaskInstanceService taskInstanceService;

    @Override
    public void run(String... args) {
        Tasks task = taskService.createTask("Sample Task", true);
        TaskInstances taskInstance = taskInstanceService.createTaskInstances(1, TaskIntervalType.DAYS, "User45", task);
        taskUpdatesService.createTaskUpdates(taskInstance, "10");
    }
}