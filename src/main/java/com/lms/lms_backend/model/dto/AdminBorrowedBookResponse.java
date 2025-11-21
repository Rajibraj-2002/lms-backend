package com.lms.lms_backend.model.dto;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data; // Import

@Data
@Builder
public class AdminBorrowedBookResponse implements Serializable { // Implement
    private static final long serialVersionUID = 1L; // Add ID

    private Long id;
    private String userName;
    private String userUsername;
    private String bookTitle;
    private LocalDate issueDate;
    private LocalDate dueDate;
}