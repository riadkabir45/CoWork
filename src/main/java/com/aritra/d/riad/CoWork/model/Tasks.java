package com.aritra.d.riad.CoWork.model;

import java.util.Set;

import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String taskName;
    private boolean isNumericalTask;
    private int taskInterval;
    private TaskIntervalType taskIntervalType;
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<TaskUpdates> updates;
}
