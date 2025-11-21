package com.lms.lms_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lms.lms_backend.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // Custom method for the login process
    Optional<User> findByUsername(String username);
    long countByRole(com.lms.lms_backend.model.enums.Role role);
}