package com.aritra.d.riad.CoWork.config;

import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;
import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.service.ConnectionService;
import com.aritra.d.riad.CoWork.service.NotificationService;
import com.aritra.d.riad.CoWork.service.PermissionService;
import com.aritra.d.riad.CoWork.service.RoleService;
import com.aritra.d.riad.CoWork.service.TaskInstanceService;
import com.aritra.d.riad.CoWork.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.aritra.d.riad.CoWork.service.TasksService;
import com.aritra.d.riad.CoWork.service.TaskUpdatesService;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private UserService userService;
    @Autowired
    private ConnectionService connectionService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TasksService taskService;
    @Autowired
    private TaskInstanceService taskInstanceService;
    @Autowired
    private TaskUpdatesService taskUpdatesService;

    @Override
    public void run(String... args) throws Exception {
        loadPermissions();
        loadRoles();
        userService.loadSupabaseUsers();
        loadUsers();
    }
    
    private void loadPermissions() {
        String[] permissions = {
            "READ_TASKS", "WRITE_TASKS", "DELETE_TASKS",
            "READ_USERS", "WRITE_USERS", "DELETE_USERS",
            "MANAGE_ROLES", "PROMOTE_MENTOR", "ASSIGN_MODERATOR",
            "BAN_USERS", "MODERATE_CONTENT"
        };
        
        for (String permissionName : permissions) {
            permissionService.createPermissionIfNotExists(permissionName, "Permission to " + permissionName.toLowerCase().replace("_", " "));
        }
    }
    
    private void loadRoles() {
        // Create UNREGISTERED role
        roleService.createRoleIfNotExists("UNREGISTERED", "Users who haven't completed registration", 
            new String[]{});
        
        // Create REGISTERED role
        roleService.createRoleIfNotExists("REGISTERED", "Registered users with basic access", 
            new String[]{"READ_TASKS", "WRITE_TASKS"});
        
        // Create MENTOR role
        roleService.createRoleIfNotExists("MENTOR", "Users who can mentor others", 
            new String[]{"READ_TASKS", "WRITE_TASKS", "READ_USERS"});
        
        // Create MODERATOR role
        roleService.createRoleIfNotExists("MODERATOR", "Users who can moderate content and manage users", 
            new String[]{"READ_TASKS", "WRITE_TASKS", "DELETE_TASKS", "READ_USERS", 
                        "WRITE_USERS", "BAN_USERS", "MODERATE_CONTENT"});
        
        // Create ADMIN role
        roleService.createRoleIfNotExists("ADMIN", "Administrators with full access", 
            new String[]{"READ_TASKS", "WRITE_TASKS", "DELETE_TASKS", "READ_USERS", 
                        "WRITE_USERS", "DELETE_USERS", "MANAGE_ROLES", "PROMOTE_MENTOR", 
                        "ASSIGN_MODERATOR", "BAN_USERS", "MODERATE_CONTENT"});
    }

    public void loadUsers() {
        // Only create users if they don't already exist
        if (userService.findByEmail("jan@example.com").isEmpty()) {
            userService.createUser("jan@example.com", "Jan", "Kowalski");
        }
        
        Users ruby;
        if (userService.findByEmail("ruby@example.com").isEmpty()) {
            ruby = userService.createUser("ruby@example.com", "Ruby", "Smith");
        } else {
            ruby = userService.findByEmail("ruby@example.com").orElseThrow();
        }
        
        if (userService.findByEmail("revere@example.com").isEmpty()) {
            userService.createUser("revere@example.com", "Revere", "Thee");
        }

        Users riad = userService.findByEmail("riadkabir45@gmail.com").orElseThrow();
        Users aritra = userService.findByEmail("aritra.chakraborty@g.bracu.ac.bd").orElseThrow();

        // Only assign admin role if user doesn't already have it
        if (!riad.hasRole("ADMIN")) {
            userService.assignAdmin(riad.getId());
        }
        if (!aritra.hasRole("ADMIN")) {
            userService.assignAdmin(aritra.getId());
        }
        
        // Only make riad mentor if not already a mentor
        if (!riad.hasRole("MENTOR")) {
            userService.promoteToMentor(riad);
        }

        Tasks task = taskService.createTask("Sample Task", true);
        TaskInstances taskInstance = taskInstanceService.createTaskInstances(5, TaskIntervalType.HOURS, riad, task);
        TaskInstances taskInstance2 = taskInstanceService.createTaskInstances(20, TaskIntervalType.HOURS, ruby, task);
        taskUpdatesService.createTaskUpdates(taskInstance, "10",LocalDateTime.now().minusHours(15));
        taskUpdatesService.createTaskUpdates(taskInstance, "10",LocalDateTime.now().minusHours(7));
        taskUpdatesService.createTaskUpdates(taskInstance, "10",LocalDateTime.now().minusHours(3));
        taskUpdatesService.createTaskUpdates(taskInstance, "1");
        taskUpdatesService.createTaskUpdates(taskInstance2, "10",LocalDateTime.now().minusHours(8));
        taskUpdatesService.createTaskUpdates(taskInstance2, "10",LocalDateTime.now().minusHours(5));
        taskUpdatesService.createTaskUpdates(taskInstance2, "10",LocalDateTime.now().minusHours(1));
        taskUpdatesService.createTaskUpdates(taskInstance2, "1");

    }

}
