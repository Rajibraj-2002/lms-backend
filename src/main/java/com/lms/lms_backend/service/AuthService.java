package com.lms.lms_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lms.lms_backend.model.dto.LibrarianContactDTO;
import com.lms.lms_backend.model.dto.LibrarianRegistrationRequest;
import com.lms.lms_backend.model.dto.UserCreationRequest;
import com.lms.lms_backend.model.entity.User;
import com.lms.lms_backend.model.enums.Role;
import com.lms.lms_backend.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminKey;

    public AuthService(UserRepository userRepository, 
                           PasswordEncoder passwordEncoder, 
                           @Value("${lms.admin.key}") String adminKey) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminKey = adminKey;
    }

    /**
     * Registers a new Librarian.
     */
    @CacheEvict(value = "admin_stats", allEntries = true)
    public User registerLibrarian(LibrarianRegistrationRequest request) {
        
        if (!request.getAuthorizationKey().equals(adminKey)) {
            throw new RuntimeException("Invalid Authorization Key");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        User librarian = new User();
        librarian.setName(request.getName());
        librarian.setLibrarianId(request.getLibrarianId());
        librarian.setEmail(request.getEmail());
        librarian.setUsername(request.getUsername());
        librarian.setPassword(passwordEncoder.encode(request.getPassword()));
        librarian.setRole(Role.LIBRARIAN);

        return userRepository.save(librarian);
    }

    /**
     * Creates a new User (Member).
     */
    @CacheEvict(value = "admin_stats", allEntries = true)
    public User createUser(UserCreationRequest request) {
        
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        User user = new User();
        user.setName(request.getName());
        user.setMobileNumber(request.getMobileNumber());
        user.setEmail(request.getEmail()); 
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    /**
     * Admin: Updates an existing User.
     */
    @Transactional
    @CacheEvict(value = "user_details", key = "#request.username")
    public User updateUser(Long userId, UserCreationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        user.setName(request.getName());
        user.setMobileNumber(request.getMobileNumber());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * Public: Resets password using the Admin Key.
     */
    @Transactional
    @CacheEvict(value = "user_details", key = "#username") 
    public void resetLibrarianPassword(String username, String key, String newPassword) {
        if (!key.equals(adminKey)) {
            throw new RuntimeException("Invalid Admin Key. Cannot reset password.");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != Role.LIBRARIAN) {
             throw new RuntimeException("Only Librarians can reset via Admin Key.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Protected: Changes password for logged-in user.
     */
    @Transactional
    @CacheEvict(value = "user_details", key = "#username")
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Incorrect old password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    @Cacheable("librarian_contact")
    public LibrarianContactDTO getLibrarianContact() {
        User librarian = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.LIBRARIAN)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No librarian account found."));
        
        return LibrarianContactDTO.builder()
                .name(librarian.getName())
                .email(librarian.getEmail())
                .mobileNumber(librarian.getMobileNumber()) // 2. ADDED THIS LINE
                .build();
    }

    public User getLibrarianUser() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.LIBRARIAN)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No librarian account found."));
    }
    @CacheEvict(value = {"admin_stats", "user_details"}, allEntries = true)
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        // Note: If the user has active books/fines, the database might block this 
        // depending on your foreign key settings. This is a safety feature.
        userRepository.deleteById(id);
    }
}