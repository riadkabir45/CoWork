package com.aritra.d.riad.CoWork.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ConnectionDTO {
    String id;
    String senderId;
    String receiverId;
    boolean accepted;
    LocalDateTime upDateTime;
}
