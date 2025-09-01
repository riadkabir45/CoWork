package com.aritra.d.riad.CoWork.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SupabaseUserDTO {
    private String id;
    private String email;
    private String phone;

    @JsonProperty("user_metadata")
    private Map<String, Object> userMetadata;

    public String getFirstName() {
        return userMetadata != null ? (String) userMetadata.get("first_name") : null;
    }

    public String getLastName() {
        return userMetadata != null ? (String) userMetadata.get("last_name") : null;
    }

    public Boolean getEmailVerified() {
        return userMetadata != null ? (Boolean) userMetadata.get("email_verified") : null;
    }

    public Boolean getPhoneVerified() {
        return userMetadata != null ? (Boolean) userMetadata.get("phone_verified") : null;
    }
}