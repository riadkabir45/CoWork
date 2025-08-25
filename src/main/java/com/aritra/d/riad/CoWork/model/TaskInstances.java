package com.aritra.d.riad.CoWork.model;

import java.util.Set;

import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskInstances {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private int taskInterval;
    private TaskIntervalType taskIntervalType;
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tasks task;

    @OneToMany(mappedBy = "taskInstances", fetch = FetchType.LAZY)
    private Set<TaskUpdates> taskUpdates;
}
