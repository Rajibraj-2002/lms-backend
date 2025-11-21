package com.lms.lms_backend.service;

import java.util.List;
import java.util.stream.Collectors; // FIX: Use your DTO

import org.springframework.stereotype.Service; // FIX: Use your DTO
import org.springframework.transaction.annotation.Transactional;

import com.lms.lms_backend.model.dto.BookResponse;
import com.lms.lms_backend.model.dto.FineResponse;
import com.lms.lms_backend.model.dto.ManualFineRequest;
import com.lms.lms_backend.model.dto.ProfileResponse;
import com.lms.lms_backend.model.entity.Book;
import com.lms.lms_backend.model.entity.BorrowedBook;
import com.lms.lms_backend.model.entity.Fine;
import com.lms.lms_backend.model.entity.Notification;
import com.lms.lms_backend.model.entity.User;
import com.lms.lms_backend.repository.BorrowedBookRepository;
import com.lms.lms_backend.repository.FineRepository;
import com.lms.lms_backend.repository.NotificationRepository;
import com.lms.lms_backend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BorrowedBookRepository borrowedBookRepository;
    private final FineRepository fineRepository;
    private final NotificationRepository notificationRepository;

    public UserService(UserRepository userRepository, BorrowedBookRepository borrowedBookRepository, 
                         FineRepository fineRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.borrowedBookRepository = borrowedBookRepository;
        this.fineRepository = fineRepository;
        this.notificationRepository = notificationRepository;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User profile not found."));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getUserProfile(String username) {
        User user = getUserByUsername(username);
        return ProfileResponse.builder()
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole())
                .build();
    }
    @Transactional
public Fine addManualFine(ManualFineRequest request) {
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found: " + request.getUsername()));

    Fine fine = new Fine();
    fine.setUser(user);
    fine.setAmount(request.getAmount());
    fine.setReason(request.getReason());
    fine.setStatus("UNPAID");
    // We leave borrowedBook as null

    return fineRepository.save(fine);
}

    @Transactional(readOnly = true)
    public List<BookResponse> getMyBorrowedBooks(String username) {
        User user = getUserByUsername(username);
        return borrowedBookRepository.findByUserIdAndReturnedDateIsNull(user.getId()).stream()
                .map(this::mapToUserBookResponse) // Use mapper
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FineResponse> getMyFines(String username) {
        User user = getUserByUsername(username);
        return fineRepository.findByUserIdAndStatus(user.getId(), "UNPAID").stream()
                .map(this::mapToUserFinesResponse) // Use mapper
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications(String username) {
        User user = getUserByUsername(username);
        return notificationRepository.findByUserIdAndIsRead(user.getId(), false);
    }

    @Transactional
    public void markNotificationsAsRead(String username) {
        User user = getUserByUsername(username);
        List<Notification> notifications = notificationRepository.findByUserIdAndIsRead(user.getId(), false);
        notifications.forEach(notif -> notif.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    // --- DTO MAPPERS (Now use your DTOs) ---
    
    private BookResponse mapToUserBookResponse(BorrowedBook borrowedBook) {
        Book book = borrowedBook.getBook();
        return BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn()) // <-- FIX: Was getIsn()
                .title(book.getTitle())
                .author(book.getAuthor())
                .coverImageUrl(book.getCoverImageUrl())
                .description(book.getDescription())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                // Add the specific loan details
                .issueDate(borrowedBook.getIssueDate()) 
                .dueDate(borrowedBook.getDueDate())
                .build();
    }
    
    private FineResponse mapToUserFinesResponse(Fine fine) {
        
        // --- FIX: Check if the book is null ---
        String bookTitle = (fine.getBorrowedBook() != null && fine.getBorrowedBook().getBook() != null)
                           ? fine.getBorrowedBook().getBook().getTitle()
                           : "N/A (Manual Fine)"; // Provide a default
                           
        String reason = fine.getReason() != null ? fine.getReason() : "Overdue book fine";
        // --- END FIX ---

        return FineResponse.builder()
                .id(fine.getId())
                .userName(fine.getUser().getName())
                .bookTitle(bookTitle) // Use the safe variable
                .amount(fine.getAmount())
                .status(fine.getStatus())
                .reason(reason) // Use the safe variable
                .build();
    }
}