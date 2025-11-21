package com.lms.lms_backend.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.lms.lms_backend.model.dto.AdminBorrowedBookResponse;
import com.lms.lms_backend.model.dto.BookIssueRequest;
import com.lms.lms_backend.model.dto.BookRequest;
import com.lms.lms_backend.model.dto.BookResponse;
import com.lms.lms_backend.model.entity.Book;
import com.lms.lms_backend.model.entity.BorrowedBook;
import com.lms.lms_backend.model.entity.Fine;
import com.lms.lms_backend.model.entity.Notification;
import com.lms.lms_backend.model.entity.User;
import com.lms.lms_backend.model.entity.Waitlist;
import com.lms.lms_backend.repository.BookRepository;
import com.lms.lms_backend.repository.BorrowedBookRepository;
import com.lms.lms_backend.repository.FineRepository;
import com.lms.lms_backend.repository.NotificationRepository;
import com.lms.lms_backend.repository.UserRepository;
import com.lms.lms_backend.repository.WaitlistRepository;

@Service
public class BookService {

    private final WaitlistRepository waitlistRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowedBookRepository borrowedBookRepository;
    private final NotificationRepository notificationRepository;
    private final FineRepository fineRepository;
    private final FileStorageService fileStorageService;
    private final AuthService authService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final int LOAN_DAYS = 14;
    private static final double FINE_PER_DAY = 0.50;

    public BookService(BookRepository bookRepository, UserRepository userRepository, 
                           BorrowedBookRepository borrowedBookRepository, 
                           NotificationRepository notificationRepository, 
                           FineRepository fineRepository, FileStorageService fileStorageService, 
                           WaitlistRepository waitlistRepository, AuthService authService, SimpMessagingTemplate messagingTemplate) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.borrowedBookRepository = borrowedBookRepository;
        this.notificationRepository = notificationRepository;
        this.fineRepository = fineRepository;
        this.fileStorageService = fileStorageService;
        this.waitlistRepository = waitlistRepository;
        this.authService = authService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    @CacheEvict(value = {"all_books", "latest_books", "search_books", "admin_stats"}, allEntries = true)
    public BookResponse addBook(BookRequest request, MultipartFile file) throws IOException {
        String fileName = fileStorageService.storeFile(file);
        String fileUrl = "/uploads/" + fileName;

        Book book = bookRepository.findByIsbn(request.getIsbn())
                .orElse(new Book()); 

        boolean isStockAdded = book.getId() != null && request.getTotalCopies() > book.getTotalCopies();

        if (book.getId() != null) { 
            int onLoan = book.getTotalCopies() - book.getAvailableCopies();
            int newTotal = request.getTotalCopies();
            book.setAvailableCopies(newTotal - onLoan);
            book.setTotalCopies(newTotal);
        } else {
            book.setTotalCopies(request.getTotalCopies());
            book.setAvailableCopies(request.getTotalCopies());
        }

        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setCoverImageUrl(fileUrl); 

        Book savedBook = bookRepository.save(book);
        
        if (isStockAdded && book.getAvailableCopies() > 0) {
            notifyWaitlist(savedBook);
        }

        return mapToBookResponse(savedBook);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = {"all_books", "search_books", "admin_stats"}, allEntries = true),
        @CacheEvict(value = {"user_books", "user_notifications"}, allEntries = true) 
    })
    public void issueBook(BookIssueRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUsername()));

        String[] isbnList = request.getIsbn().split(",");
        
        if (isbnList.length == 0) {
            throw new RuntimeException("No ISBN provided.");
        }

        StringBuilder errorMessages = new StringBuilder();
        int successCount = 0;

        for (String isbnStr : isbnList) {
            String isbn = isbnStr.trim();
            if (isbn.isEmpty()) continue;

            try {
                Book book = bookRepository.findByIsbn(isbn)
                        .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));

                if (book.getAvailableCopies() <= 0) {
                    throw new RuntimeException("Book is unavailable: " + book.getTitle());
                }

                book.setAvailableCopies(book.getAvailableCopies() - 1);
                bookRepository.save(book);

                BorrowedBook borrowedBook = new BorrowedBook();
                borrowedBook.setBook(book);
                borrowedBook.setUser(user);
                borrowedBook.setIssueDate(LocalDate.now());
                borrowedBook.setDueDate(LocalDate.now().plusDays(LOAN_DAYS));
                borrowedBookRepository.save(borrowedBook);

                Notification notification = new Notification();
                notification.setUser(user);
                notification.setMessage("The book '" + book.getTitle() + "' has been issued to you. Due date: " + borrowedBook.getDueDate());
                notification.setRead(false);
                notificationRepository.save(notification);

                messagingTemplate.convertAndSendToUser(
                    user.getUsername(), 
                    "/queue/notifications", 
                    notification
                );
                successCount++;

            } catch (RuntimeException e) {
                errorMessages.append(e.getMessage()).append("\n");
            }
        }

        if (errorMessages.length() > 0) {
            throw new RuntimeException("Completed with errors:\n" + 
                                       successCount + " books issued.\n" + 
                                       errorMessages.toString());
        }
    }
    
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = {"all_books", "search_books", "admin_stats"}, allEntries = true),
        @CacheEvict(value = {"user_books", "user_fines"}, allEntries = true)
    })
    public String returnBook(String isbn, String username) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));

        BorrowedBook borrowedRecord = borrowedBookRepository.findAll().stream()
                .filter(b -> b.getBook().getIsbn().equals(isbn) 
                          && b.getUser().getUsername().equals(username)
                          && b.getReturnedDate() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active loan found for this book and user."));

        double fineAmount = calculateFine(borrowedRecord.getDueDate(), LocalDate.now());

        if (fineAmount > 0) {
            Fine fine = new Fine();
            fine.setUser(borrowedRecord.getUser());
            fine.setBorrowedBook(borrowedRecord);
            fine.setAmount(fineAmount);
            fine.setStatus("UNPAID");
            fineRepository.save(fine);
        }

        borrowedRecord.setReturnedDate(LocalDate.now());
        borrowedBookRepository.save(borrowedRecord);

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        Book savedBook = bookRepository.save(book);
        
        if (savedBook.getAvailableCopies() == 1) {
            notifyWaitlist(savedBook);
        }
        
        return fineAmount > 0 ? 
               String.format("Book returned successfully. Fine applied: ₹%.2f", fineAmount) :
               "Book returned successfully. No fines applied.";
    }

    @Transactional(readOnly = true)
    public List<AdminBorrowedBookResponse> getAllCurrentlyBorrowedBooks() {
        return borrowedBookRepository.findByReturnedDateIsNull()
                .stream()
                .map(this::mapToAdminBorrowedResponse)
                .collect(Collectors.toList());
    }

    @Cacheable("all_books")
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::mapToBookResponse) 
                .collect(Collectors.toList());
    }

    @Transactional
    public void requestToBorrow(Long bookId, @NonNull String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found."));

        User librarian = authService.getLibrarianUser();

        Notification notif = new Notification();
        notif.setUser(librarian); 
        notif.setMessage("User '" + user.getUsername() + "' wishes to borrow the in-stock book: '" + book.getTitle() + "'.");
        notif.setRead(false);
        notificationRepository.save(notif);
        
        messagingTemplate.convertAndSendToUser(
            librarian.getUsername(), 
            "/queue/notifications", 
            notif
        );
    }

    @Transactional(readOnly = true)
    @Cacheable("latest_books")
    public List<BookResponse> getLatestBooks() {
        return bookRepository.findTop4ByOrderByIdDesc().stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    private double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (returnDate.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
            return daysOverdue * FINE_PER_DAY;
        }
        return 0.0;
    }

    @Cacheable(value = "search_books", key = "#query")
    public List<BookResponse> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrIsbnContainingIgnoreCase(query, query)
                .stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void addToWaitlist(@NonNull Long bookId, @NonNull String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found."));

        if (book.getAvailableCopies() > 0) {
            throw new RuntimeException("Book is already in stock.");
        }
        if (waitlistRepository.existsByUserIdAndBookId(user.getId(), bookId)) {
            throw new RuntimeException("You are already on the waitlist for this book.");
        }

        Waitlist waitlist = new Waitlist();
        waitlist.setUser(user);
        waitlist.setBook(book);
        waitlistRepository.save(waitlist);

        try {
            User librarian = authService.getLibrarianUser();
            Notification notif = new Notification();
            notif.setUser(librarian); 
            notif.setMessage("User '" + user.getUsername() + "' is on the waitlist for '" + book.getTitle() + "'.");
            notif.setRead(false);
            notificationRepository.save(notif);

            messagingTemplate.convertAndSendToUser(
                librarian.getUsername(), 
                "/queue/notifications", 
                notif
            );
        } catch (RuntimeException e) {
            System.err.println("Could not send waitlist notification to librarian: " + e.getMessage());
        }
    }

    private void notifyWaitlist(Book book) {
        List<Waitlist> waitlist = waitlistRepository.findByBookId(book.getId());
        if (waitlist.isEmpty()) {
            return;
        }

        for (Waitlist item : waitlist) {
            Notification notification = new Notification();
            notification.setUser(item.getUser());
            notification.setMessage("Good news! The book '" + book.getTitle() + "' is now back in stock.");
            notification.setRead(false);
            notificationRepository.save(notification);
            
            messagingTemplate.convertAndSendToUser(
                item.getUser().getUsername(),
                "/queue/notifications",
                notification
            );
        }
        
        waitlistRepository.deleteAll(waitlist);
    }
    
    private BookResponse mapToBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .coverImageUrl(book.getCoverImageUrl())
                .description(book.getDescription())
                .build();
    }

    private AdminBorrowedBookResponse mapToAdminBorrowedResponse(BorrowedBook book) {
        return AdminBorrowedBookResponse.builder()
                .id(book.getId())
                .userName(book.getUser().getName())
                .userUsername(book.getUser().getUsername())
                .bookTitle(book.getBook().getTitle())
                .issueDate(book.getIssueDate())
                .dueDate(book.getDueDate())
                .build();
    }
}