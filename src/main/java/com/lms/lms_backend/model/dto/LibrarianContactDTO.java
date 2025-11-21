package com.lms.lms_backend.model.dto;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class LibrarianContactDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String email;
    private String mobileNumber; // 1. ADDED THIS FIELD
}