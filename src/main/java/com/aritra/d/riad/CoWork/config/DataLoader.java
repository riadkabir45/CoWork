package com.aritra.d.riad.CoWork.config;

import com.aritra.d.riad.CoWork.service.PermissionService;
import com.aritra.d.riad.CoWork.service.RoleService;
import com.aritra.d.riad.CoWork.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


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
        userService.createUser("jan@example.com", "Jan", "Kowalski");
        userService.createUser("ruby@example.com", "Ruby", "Smith");
        userService.createUser("revere@example.com", "Revere", "Thee");
    }

}
