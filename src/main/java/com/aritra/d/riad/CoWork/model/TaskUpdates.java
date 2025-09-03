package com.aritra.d.riad.CoWork.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
public class TaskUpdates {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @NonNull
    private LocalDateTime updateTimestamp = LocalDateTime.now();
    private String updateDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    private TaskInstances taskInstances;

}
