package com.lms.lms_backend.repository;

import com.lms.lms_backend.model.entity.Notification;
import com.lms.lms_backend.model.entity.User; // 1. Add User import
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // --- 2. THIS IS FOR THE USER DASHBOARD ---
    List<Notification> findByUserIdAndIsRead(Long userId, boolean isRead);

    // --- 3. THIS IS FOR THE ADMIN CONTROLLER ---
    List<Notification> findByUserAndIsRead(User user, boolean isRead);
}