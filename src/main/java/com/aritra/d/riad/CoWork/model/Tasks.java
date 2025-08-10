package com.aritra.d.riad.CoWork.model;

import java.util.Set;

import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String taskName;
    private boolean isNumericalTask;
    private int taskInterval;
    private TaskIntervalType taskIntervalType;
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TaskUpdates> updates;
}
