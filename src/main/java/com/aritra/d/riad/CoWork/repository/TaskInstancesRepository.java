package com.aritra.d.riad.CoWork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.TaskInstances;

@Repository
public interface TaskInstancesRepository extends JpaRepository<TaskInstances, String> {
    java.util.List<TaskInstances> findByUserId(String userId);
}
