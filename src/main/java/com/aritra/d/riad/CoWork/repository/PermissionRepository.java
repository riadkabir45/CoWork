package com.aritra.d.riad.CoWork.repository;

import com.aritra.d.riad.CoWork.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    Optional<Permission> findByName(String name);
    boolean existsByName(String name);
}
