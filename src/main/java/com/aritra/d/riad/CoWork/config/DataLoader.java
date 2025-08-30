package com.aritra.d.riad.CoWork.config;

import com.aritra.d.riad.CoWork.model.Permission;
import com.aritra.d.riad.CoWork.model.Role;
import com.aritra.d.riad.CoWork.repository.PermissionRepository;
import com.aritra.d.riad.CoWork.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        loadPermissions();
        loadRoles();
    }
    
    @Transactional
    private void loadPermissions() {
        String[] permissions = {
            "READ_TASKS", "WRITE_TASKS", "DELETE_TASKS",
            "READ_USERS", "WRITE_USERS", "DELETE_USERS",
            "MANAGE_ROLES", "PROMOTE_MENTOR", "ASSIGN_MODERATOR",
            "BAN_USERS", "MODERATE_CONTENT"
        };
        
        for (String permissionName : permissions) {
            if (!permissionRepository.existsByName(permissionName)) {
                Permission permission = new Permission();
                permission.setName(permissionName);
                permission.setDescription("Permission to " + permissionName.toLowerCase().replace("_", " "));
                permissionRepository.save(permission);
                log.info("Created permission: {}", permissionName);
            }
        }
    }
    
    @Transactional
    private void loadRoles() {
        // Create UNREGISTERED role
        createRoleIfNotExists("UNREGISTERED", "Users who haven't completed registration", 
            new String[]{});
        
        // Create REGISTERED role
        createRoleIfNotExists("REGISTERED", "Registered users with basic access", 
            new String[]{"READ_TASKS", "WRITE_TASKS"});
        
        // Create MENTOR role
        createRoleIfNotExists("MENTOR", "Users who can mentor others", 
            new String[]{"READ_TASKS", "WRITE_TASKS", "READ_USERS"});
        
        // Create MODERATOR role
        createRoleIfNotExists("MODERATOR", "Users who can moderate content and manage users", 
            new String[]{"READ_TASKS", "WRITE_TASKS", "DELETE_TASKS", "READ_USERS", 
                        "WRITE_USERS", "BAN_USERS", "MODERATE_CONTENT"});
        
        // Create ADMIN role
        createRoleIfNotExists("ADMIN", "Administrators with full access", 
            new String[]{"READ_TASKS", "WRITE_TASKS", "DELETE_TASKS", "READ_USERS", 
                        "WRITE_USERS", "DELETE_USERS", "MANAGE_ROLES", "PROMOTE_MENTOR", 
                        "ASSIGN_MODERATOR", "BAN_USERS", "MODERATE_CONTENT"});
    }
    
    @Transactional
    private void createRoleIfNotExists(String roleName, String description, String[] permissionNames) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            role.setDescription(description);
            
            // Get permissions
            Set<Permission> permissions = new HashSet<>();
            for (String permissionName : permissionNames) {
                permissionRepository.findByName(permissionName)
                    .ifPresent(permissions::add);
            }
            role.setPermissions(permissions);
            
            // Save the role first
            role = roleRepository.save(role);
            log.info("Created role: {} with {} permissions", roleName, permissions.size());
        }
    }
}
