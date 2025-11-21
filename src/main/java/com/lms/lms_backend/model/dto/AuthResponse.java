package com.lms.lms_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // Lombok annotation to create a constructor
public class AuthResponse {
    private String jwt;
    private String role;
}