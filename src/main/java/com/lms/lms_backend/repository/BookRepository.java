package com.lms.lms_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lms.lms_backend.model.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);
    
    List<Book> findTop4ByOrderByIdDesc(); 

    // --- NEW: For Typeahead Search ---
    // Finds books where Title OR ISBN contains the search term (case-insensitive)
    List<Book> findByTitleContainingIgnoreCaseOrIsbnContainingIgnoreCase(String titleQuery, String isbnQuery);
}