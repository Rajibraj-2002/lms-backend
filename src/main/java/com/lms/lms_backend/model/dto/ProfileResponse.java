package com.lms.lms_backend.model.dto;

import java.io.Serializable;

import com.lms.lms_backend.model.enums.Role;

import lombok.Builder;
import lombok.Data; // Import

@Data
@Builder
public class ProfileResponse implements Serializable { // Implement
    private static final long serialVersionUID = 1L; // Add ID

    private String name;
    private String username;
    private String email;
    private String mobileNumber;
    private Role role;
}