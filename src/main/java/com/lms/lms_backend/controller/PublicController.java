package com.lms.lms_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lms.lms_backend.model.dto.BookResponse;
import com.lms.lms_backend.model.dto.LibrarianContactDTO;
import com.lms.lms_backend.service.AuthService;
import com.lms.lms_backend.service.BookService;

@RestController
@RequestMapping("/api/public") 
@CrossOrigin(origins = "*")
public class PublicController {

    private final BookService bookService;
    private final AuthService authService;

    public PublicController(BookService bookService, AuthService authService) {
        this.bookService = bookService;
        this.authService = authService;
    }

    @GetMapping("/librarian-contact")
        public ResponseEntity<LibrarianContactDTO> getLibrarianContact() {
        LibrarianContactDTO contact = authService.getLibrarianContact();
        return ResponseEntity.ok(contact);
    }
    @GetMapping("/books/latest")
    public ResponseEntity<List<BookResponse>> getLatestBooks() {
        List<BookResponse> books = bookService.getLatestBooks();
        return ResponseEntity.ok(books);
    }

    // --- NEW: API for all users to get all books ---
    @GetMapping("/books/all")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    // --- NEW: API for all users to search books ---
    @GetMapping("/books/search")
    public ResponseEntity<List<BookResponse>> searchBooks(@RequestParam String query) {
        List<BookResponse> books = bookService.searchBooks(query);
        return ResponseEntity.ok(books);
    }
}