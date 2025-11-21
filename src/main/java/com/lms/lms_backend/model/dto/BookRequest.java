package com.lms.lms_backend.model.dto;

import lombok.Data;

@Data
public class BookRequest {
    private String isbn;
    private String title;
    private String author;
    private int totalCopies;
    private String description;
    
}