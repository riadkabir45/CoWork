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
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne
    private Connections connections;
    @ManyToOne
    private Users sender;
    private String content;
    private boolean seen;
    private LocalDateTime createdAt = LocalDateTime.now();
}
