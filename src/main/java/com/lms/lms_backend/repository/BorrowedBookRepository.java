package com.lms.lms_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lms.lms_backend.model.entity.BorrowedBook;

public interface BorrowedBookRepository extends JpaRepository<BorrowedBook, Long> {

    // Finds all records for a user where the returned_date column is null
    List<BorrowedBook> findByUserIdAndReturnedDateIsNull(Long userId);

    // --- ADD THIS METHOD ---
    // Finds all records (for any user) where the book has not been returned
    List<BorrowedBook> findByReturnedDateIsNull();
    long countByReturnedDateIsNull();
}