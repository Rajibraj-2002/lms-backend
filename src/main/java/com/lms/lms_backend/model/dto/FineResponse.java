package com.lms.lms_backend.model.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data; // Import

@Data
@Builder
public class FineResponse implements Serializable { // Implement
    private static final long serialVersionUID = 1L; // Add ID
    
    private Long id; 
    private String userName;
    private String bookTitle;
    private Double amount;
    private String status;
    private String reason;
}