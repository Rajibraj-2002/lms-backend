package com.lms.lms_backend.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.lms_backend.model.dto.AdminBorrowedBookResponse;
import com.lms.lms_backend.model.dto.BookIssueRequest;
import com.lms.lms_backend.model.dto.BookRequest;
import com.lms.lms_backend.model.dto.BookResponse;
import com.lms.lms_backend.model.dto.BookReturnRequest;
import com.lms.lms_backend.model.dto.ChangePasswordRequest;
import com.lms.lms_backend.model.dto.DashboardStatsDTO;
import com.lms.lms_backend.model.dto.FineResponse;
import com.lms.lms_backend.model.dto.ManualFineRequest; // Import ManualFineRequest
import com.lms.lms_backend.model.dto.UserCreationRequest;
import com.lms.lms_backend.model.entity.Notification;
import com.lms.lms_backend.model.entity.User;
import com.lms.lms_backend.repository.NotificationRepository;
import com.lms.lms_backend.service.AuthService;
import com.lms.lms_backend.service.BookService;
import com.lms.lms_backend.service.DashboardService;
import com.lms.lms_backend.service.FineService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AuthService authService;
    private final BookService bookService;
    private final FineService fineService;
    private final DashboardService dashboardService;
    private final NotificationRepository notificationRepository;

    public AdminController(AuthService authService, BookService bookService, 
                           FineService fineService, DashboardService dashboardService,
                           NotificationRepository notificationRepository) {
        this.authService = authService;
        this.bookService = bookService;
        this.fineService = fineService;
        this.dashboardService = dashboardService;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping("/users/create")
    public ResponseEntity<?> createUser(@RequestBody UserCreationRequest request) {
        try {
            User user = authService.createUser(request);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/users/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserCreationRequest request) {
        try {
            User user = authService.updateUser(id, request);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/books/add")
    public ResponseEntity<?> addBook(
            @RequestParam("file") MultipartFile file,
            @RequestParam("book") String bookRequestJson) {
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BookRequest request = objectMapper.readValue(bookRequestJson, BookRequest.class);

            BookResponse book = bookService.addBook(request, file);
            return new ResponseEntity<>(book, HttpStatus.CREATED);
            
        } catch (JsonProcessingException e) { 
            return new ResponseEntity<>("Invalid book data format (JSON error).", HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>("Could not save the uploaded file.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) { 
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/books/issue")
    public ResponseEntity<?> issueBook(@RequestBody BookIssueRequest request) {
        try {
            bookService.issueBook(request);
            return ResponseEntity.ok("Book issued successfully. Notification sent to user.");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/books/search")
    public ResponseEntity<List<BookResponse>> searchBooks(@RequestParam String query) {
        List<BookResponse> books = bookService.searchBooks(query);
        return ResponseEntity.ok(books);
    }
    
    @PostMapping("/books/return")
    public ResponseEntity<?> returnBook(@RequestBody BookReturnRequest request) {
        try {
            String resultMessage = bookService.returnBook(request.getIsbn(), request.getUsername());
            return ResponseEntity.ok(resultMessage);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/books/all")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/users/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/fines/all")
    public ResponseEntity<List<FineResponse>> getAllOutstandingFines() {
        List<FineResponse> fines = fineService.getAllOutstandingFines();
        return ResponseEntity.ok(fines);
    }
    
    @GetMapping("/borrowed/all")
    public ResponseEntity<List<AdminBorrowedBookResponse>> getAllCurrentlyBorrowedBooks() {
        List<AdminBorrowedBookResponse> borrowed = bookService.getAllCurrentlyBorrowedBooks(); 
        return ResponseEntity.ok(borrowed);
    }

    @PostMapping("/fines/pay")
    public ResponseEntity<?> payFine(@RequestParam @NonNull Long fineId) {
        try {
            fineService.payFine(fineId);
            return ResponseEntity.ok("Fine ID " + fineId + " successfully marked as PAID.");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    // --- FIX: Re-added Manual Fine Endpoint ---
    @PostMapping("/fines/add")
    public ResponseEntity<?> addManualFine(@RequestBody ManualFineRequest request) {
        try {
            fineService.addManualFine(request);
            return ResponseEntity.ok("Manual fine added successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-notifications")
    public ResponseEntity<List<Notification>> getLibrarianNotifications() {
        User librarian = authService.getLibrarianUser();
        List<Notification> notifications = notificationRepository.findByUserAndIsRead(librarian, false);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/my-notifications/mark-read")
    public ResponseEntity<?> markLibrarianNotificationsAsRead() {
        User librarian = authService.getLibrarianUser();
        List<Notification> notifications = notificationRepository.findByUserAndIsRead(librarian, false);
        for (Notification notif : notifications) {
            notif.setRead(true);
        }
        notificationRepository.saveAll(notifications);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            authService.changePassword(userDetails.getUsername(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}