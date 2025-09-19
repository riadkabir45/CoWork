package com.aritra.d.riad.CoWork.model;

import java.time.LocalDateTime;

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
    private String id;

    @ManyToOne
    private Users sender;

    @ManyToOne
    private Users receiver;

    private boolean accepted = false;

    private LocalDateTime updatedAt = LocalDateTime.now();

    private int rateing = 0;

    @PreUpdate
    private void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
