package com.aritra.d.riad.CoWork.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aritra.d.riad.CoWork.model.Permission;
import com.aritra.d.riad.CoWork.repository.PermissionRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public void createPermissionIfNotExists(String permissionName, String description) {
        if (!permissionRepository.existsByName(permissionName)) {
            Permission permission = new Permission();
            permission.setName(permissionName);
            permission.setDescription(description);
            permissionRepository.save(permission);
            log.info("Created permission: {}", permissionName);
        }
    }

    public Optional<Permission> checkExists(String permissionName) {
        return permissionRepository.findByName(permissionName);
    }
}
