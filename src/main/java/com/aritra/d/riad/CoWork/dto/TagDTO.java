package com.aritra.d.riad.CoWork.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
    private String id;
    private String name;
    private String description;
    private String color;
    private boolean approved;
}