package com.aritra.d.riad.CoWork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.model.Tasks;

@Repository
public interface TaskInstancesRepository extends JpaRepository<TaskInstances, String> {
    java.util.List<TaskInstances> findByUserId(String userId);
    java.util.List<TaskInstances> findByUserEmail(String email);
    List<TaskInstances> findByTaskOrderByTaskStreakDesc(Tasks task);
}
