package com.aritra.d.riad.CoWork.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String title;
    private String message;
    @ManyToOne
    private Users recipient;
    private boolean read = false;
    private LocalDateTime timestamp = LocalDateTime.now();
}
