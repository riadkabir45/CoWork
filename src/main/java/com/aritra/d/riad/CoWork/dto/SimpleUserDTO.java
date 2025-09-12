package com.aritra.d.riad.CoWork.dto;

import lombok.Data;

@Data
public class SimpleUserDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private boolean hasPublicProfile;
}
