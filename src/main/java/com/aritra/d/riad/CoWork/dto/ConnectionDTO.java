package com.aritra.d.riad.CoWork.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ConnectionDTO {
    String id;
    SimpleUserDTO sender;
    SimpleUserDTO receiver;
    boolean accepted;
    LocalDateTime upDateTime;
}
