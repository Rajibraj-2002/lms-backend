package com.lms.lms_backend.model.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data; // 1. IMPORT THIS

@Data
@Builder
public class DashboardStatsDTO implements Serializable { // 2. IMPLEMENT SERIALIZABLE
    private static final long serialVersionUID = 1L; // 3. ADD ID

    private long totalMembers;
    private long totalBooks;
    private long booksOnLoan;
    private double totalFinesAmount;
}