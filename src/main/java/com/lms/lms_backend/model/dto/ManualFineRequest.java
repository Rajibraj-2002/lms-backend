package com.lms.lms_backend.model.dto;
import lombok.Data;

@Data
public class ManualFineRequest {
    private String username;
    private Double amount;
    private String reason;
}