package com.lms.lms_backend.model.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}