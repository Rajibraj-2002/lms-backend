package com.lms.lms_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lms.lms_backend.model.dto.FineResponse;
import com.lms.lms_backend.model.dto.ManualFineRequest;
import com.lms.lms_backend.model.entity.Fine;
import com.lms.lms_backend.model.entity.User;
import com.lms.lms_backend.repository.FineRepository;
import com.lms.lms_backend.repository.UserRepository;

@Service
public class FineService {

    private final FineRepository fineRepository;
    private final UserRepository userRepository;

    @Autowired
    public FineService(FineRepository fineRepository, UserRepository userRepository) { 
        this.fineRepository = fineRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    // Clear admin list, ALL user fines (to be safe), and admin stats
    @CacheEvict(value = {"admin_fines", "user_fines", "admin_stats"}, allEntries = true)
    public void payFine(@NonNull Long fineId) {
        fineRepository.findById(fineId)
                .ifPresentOrElse(fine -> {
                    if (fine.getStatus().equals("UNPAID")) {
                        fine.setStatus("PAID");
                        fineRepository.save(fine);
                    } else {
                        throw new RuntimeException("Fine is already paid.");
                    }
                }, () -> {
                    throw new RuntimeException("Fine not found.");
                });
    }

    @Transactional(readOnly = true)
    // Cache the massive list of admin fines
    @Cacheable("admin_fines")
    public List<FineResponse> getAllOutstandingFines() {
        return fineRepository.findByStatus("UNPAID").stream()
                .map(this::mapToFineResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    // Clear specific user's fine cache and admin caches
    @CacheEvict(value = {"admin_fines", "admin_stats"}, allEntries = true)
    public Fine addManualFine(ManualFineRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUsername()));

        Fine fine = new Fine();
        fine.setUser(user);
        fine.setAmount(request.getAmount());
        fine.setReason(request.getReason()); 
        fine.setStatus("UNPAID");

        return fineRepository.save(fine);
    }

    private FineResponse mapToFineResponse(com.lms.lms_backend.model.entity.Fine fine) {
        String bookTitle = (fine.getBorrowedBook() != null && fine.getBorrowedBook().getBook() != null)
                           ? fine.getBorrowedBook().getBook().getTitle()
                           : null; 
                           
        String userName = fine.getUser() != null ? fine.getUser().getName() : "Unknown User";
        String reason = fine.getReason() != null ? fine.getReason() : "Overdue book fine";

        return FineResponse.builder()
                .id(fine.getId())
                .userName(userName)
                .bookTitle(bookTitle) 
                .amount(fine.getAmount())
                .status(fine.getStatus())
                .reason(reason) 
                .build();
    }
}