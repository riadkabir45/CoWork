package com.aritra.d.riad.CoWork.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aritra.d.riad.CoWork.model.TaskUpdates;

public interface TaskUpdatesRepository extends JpaRepository<TaskUpdates, String> {
    
}
