package com.lms.lms_backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.jpa.repository.JpaRepository; // Keep this import if you use Optional elsewhere (or remove it)

import com.lms.lms_backend.model.entity.Fine;

public interface FineRepository extends JpaRepository<Fine, Long> {

    // Custom method to get all unpaid fines for a specific user (Existing)
    List<Fine> findByUserIdAndStatus(Long userId, String status);

    // NEW: Find all fines by status (for admin list view)
    List<Fine> findByStatus(String status);
    long countByStatus(String status);
    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.status = 'UNPAID'")
    Double sumUnpaidFines();
    // The findById(Long id) method is now inherited and works perfectly.
    // We have removed the line that was causing the error.
}