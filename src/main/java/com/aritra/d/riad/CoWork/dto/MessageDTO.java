package com.aritra.d.riad.CoWork.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MessageDTO {
    private String id;
    private String connectionsId;
    private String senderEmail;
    private String content;
    private boolean seen;
    private LocalDateTime createdAt = LocalDateTime.now();
}
