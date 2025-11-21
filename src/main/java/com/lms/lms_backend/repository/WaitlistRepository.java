package com.lms.lms_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lms.lms_backend.model.entity.Waitlist;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    // Find all users waiting for a specific book
    List<Waitlist> findByBookId(Long bookId);

    // Check if a user is already on the waitlist for a book
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}