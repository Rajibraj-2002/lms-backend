package com.lms.lms_backend.model.dto;

import lombok.Data;

@Data
public class UserCreationRequest {
    private String name;
    private String mobileNumber;
    private String username;
    private String password;
    private String email;
}