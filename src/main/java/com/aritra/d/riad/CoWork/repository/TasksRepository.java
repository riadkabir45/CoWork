package com.aritra.d.riad.CoWork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.Tasks;

@Repository
public interface TasksRepository extends JpaRepository<Tasks, String> {
}
