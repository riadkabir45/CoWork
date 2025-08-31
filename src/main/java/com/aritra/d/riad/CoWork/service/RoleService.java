package com.aritra.d.riad.CoWork.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aritra.d.riad.CoWork.model.Permission;
import com.aritra.d.riad.CoWork.model.Role;
import com.aritra.d.riad.CoWork.repository.RoleRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionService permissionService;


    public void createRoleIfNotExists(String roleName, String description, String[] permissionNames) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            role.setDescription(description);
            
            // Get permissions
            Set<Permission> permissions = new HashSet<>();
            for (String permissionName : permissionNames) {
                permissionService.checkExists(permissionName)
                    .ifPresent(permissions::add);
            }
            role.setPermissions(permissions);
            
            // Save the role first
            role = roleRepository.save(role);
            log.info("Created role: {} with {} permissions", roleName, permissions.size());
        }
    }
}
