package com.aritra.d.riad.CoWork.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Data
@Entity
public class Connections {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Users sender;

    @ManyToOne
    private Users receiver;

    private boolean accepted = false;

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    private void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
