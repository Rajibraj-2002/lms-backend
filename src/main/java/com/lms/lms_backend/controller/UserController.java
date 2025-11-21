package com.lms.lms_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity; // FIX: Use BookResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal; // FIX: Use FineResponse
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lms.lms_backend.model.dto.BookResponse;
import com.lms.lms_backend.model.dto.FineResponse;
import com.lms.lms_backend.model.dto.ProfileResponse;
import com.lms.lms_backend.model.dto.WaitlistRequest;
import com.lms.lms_backend.model.entity.Notification;
import com.lms.lms_backend.service.BookService;
import com.lms.lms_backend.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final BookService bookService;

    public UserController(UserService userService, BookService bookService) {
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/my-profile")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserProfile(userDetails.getUsername()));
    }

    @PostMapping("/waitlist/add")
    public ResponseEntity<?> addToWaitlist(@RequestBody WaitlistRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            bookService.addToWaitlist(request.getBookId(), userDetails.getUsername());
            return ResponseEntity.ok("Added to waitlist!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/request-borrow")
    public ResponseEntity<?> requestToBorrow(@RequestBody WaitlistRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            bookService.requestToBorrow(request.getBookId(), userDetails.getUsername());
            return ResponseEntity.ok("Request sent to librarian!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // --- UPDATED ENDPOINTS ---

    @GetMapping("/my-books")
    public ResponseEntity<List<BookResponse>> getMyBooks(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyBorrowedBooks(userDetails.getUsername()));
    }

    @GetMapping("/my-fines")
    public ResponseEntity<List<FineResponse>> getMyFines(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyFines(userDetails.getUsername()));
    }

    @GetMapping("/my-notifications")
    public ResponseEntity<List<Notification>> getMyNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyNotifications(userDetails.getUsername()));
    }

    @PostMapping("/my-notifications/mark-read")
    public ResponseEntity<?> markMyNotificationsAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        userService.markNotificationsAsRead(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    
}