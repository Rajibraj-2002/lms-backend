package com.lms.lms_backend.model.dto;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data; // 1. ADD THIS IMPORT

@Data
@Builder
public class BookResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;
    private String description;
    private String coverImageUrl; 
    
    // --- 2. ADD THESE FIELDS ---
    private LocalDate issueDate;
    private LocalDate dueDate;
}