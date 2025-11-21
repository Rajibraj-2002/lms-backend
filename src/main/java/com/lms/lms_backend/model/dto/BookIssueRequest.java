package com.lms.lms_backend.model.dto;

import lombok.Data;

@Data
public class BookIssueRequest {
    private String isbn;
    private String username;
    // We don't need 'number of books' from the form,
    // as issuing one ISBN at a time is safer.
}