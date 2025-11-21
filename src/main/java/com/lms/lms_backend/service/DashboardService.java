package com.lms.lms_backend.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.lms.lms_backend.model.dto.DashboardStatsDTO;
import com.lms.lms_backend.model.enums.Role;
import com.lms.lms_backend.repository.BookRepository;
import com.lms.lms_backend.repository.BorrowedBookRepository;
import com.lms.lms_backend.repository.FineRepository; // Import
import com.lms.lms_backend.repository.UserRepository;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowedBookRepository borrowedBookRepository;
    private final FineRepository fineRepository;

    public DashboardService(UserRepository userRepository, BookRepository bookRepository, 
                            BorrowedBookRepository borrowedBookRepository, FineRepository fineRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.borrowedBookRepository = borrowedBookRepository;
        this.fineRepository = fineRepository;
    }

    // Cache the dashboard stats
    @Cacheable("admin_stats")
    public DashboardStatsDTO getAdminDashboardStats() {
        long totalMembers = userRepository.countByRole(Role.USER);
        long totalBooks = bookRepository.count();
        long booksOnLoan = borrowedBookRepository.countByReturnedDateIsNull();
        
        Double unpaidFinesSum = fineRepository.sumUnpaidFines();
        double totalUnpaidAmount = (unpaidFinesSum == null) ? 0.0 : unpaidFinesSum;

        return DashboardStatsDTO.builder()
                .totalMembers(totalMembers)
                .totalBooks(totalBooks)
                .booksOnLoan(booksOnLoan)
                .totalFinesAmount(totalUnpaidAmount)
                .build();
    }
}