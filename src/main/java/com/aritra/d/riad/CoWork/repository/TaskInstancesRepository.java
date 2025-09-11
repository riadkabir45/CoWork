package com.aritra.d.riad.CoWork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.model.Tasks;

@Repository
public interface TaskInstancesRepository extends JpaRepository<TaskInstances, String> {
    java.util.List<TaskInstances> findByUserId(String userId);
    java.util.List<TaskInstances> findByUserEmail(String email);
    List<TaskInstances> findByTaskOrderByTaskStreakDesc(Tasks task);
    
    @Query("SELECT COUNT(t) FROM TaskInstances t WHERE t.user.id = :userId")
    Long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(t) FROM TaskInstances t WHERE t.user.id = :userId AND t.taskStreak > 0")
    Long countCompletedTasksByUserId(@Param("userId") String userId);
    
    @Query("SELECT COALESCE(MAX(t.taskStreak), 0) FROM TaskInstances t WHERE t.user.id = :userId")
    Integer getCurrentStreakForUser(@Param("userId") String userId);
}
