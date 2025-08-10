package com.aritra.d.riad.CoWork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.aritra.d.riad.CoWork.service.TaskService;

@Component
public class Runner implements CommandLineRunner {

    @Autowired
    private TaskService taskService;

    @Override
    public void run(String... args) {
        taskService.createTask("Sample Task", true, "Daily");
    }
}