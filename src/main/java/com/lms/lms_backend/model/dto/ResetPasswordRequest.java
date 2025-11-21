package com.lms.lms_backend.model.dto;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String username;
    private String adminKey; // Security check
    private String newPassword;
}