package com.aritra.d.riad.CoWork.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import jakarta.persistence.PrePersist;

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

    @Column(unique = true)
    private String hash;

    @PrePersist
    public void computeHash() {
        if (recipient != null && title != null && message != null) {
            hash = Integer.toHexString(
                recipient.getId().hashCode() + title.hashCode() + message.hashCode()
            );
        }
    }
}
