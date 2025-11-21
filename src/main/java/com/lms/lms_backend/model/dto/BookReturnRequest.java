package com.lms.lms_backend.model.dto;

import lombok.Data;

@Data
public class BookReturnRequest {
    private String isbn;
    private String username; // --- NEW FIELD ---
}