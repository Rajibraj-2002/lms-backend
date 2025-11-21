package com.lms.lms_backend.model.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class BorrowedBookResponse {
    private String bookTitle;
    private String bookIsbn;
    private LocalDate issueDate;
    private LocalDate dueDate;
}