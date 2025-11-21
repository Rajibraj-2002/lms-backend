package com.lms.lms_backend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import com.lms.lms_backend.model.entity.BorrowedBook;

@Entity
@Table(name = "fines")
@Getter @Setter
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // --- FIX IS HERE ---
    // 1. Change the column name to match your database error
    // 2. Ensure nullable = true is set
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "borrow_id", nullable = true) 
    private BorrowedBook borrowedBook;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String status; // e.g., "UNPAID", "PAID"
    
    private String reason;
}