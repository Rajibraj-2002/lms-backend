package com.lms.lms_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lms.lms_backend.model.entity.User;
import com.lms.lms_backend.model.enums.Role;
import com.lms.lms_backend.repository.UserRepository;

//@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // --- THIS IS THE USER WE WILL LOG IN WITH ---
        String adminUsername = "admin";
        String adminPassword = "admin";

        // 1. Check if the admin user already exists
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            
            // 2. If not, create a new User entity
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode(adminPassword)); // Encrypt the password
            adminUser.setName("Administrator");
            adminUser.setRole(Role.LIBRARIAN); // Assign the LIBRARIAN role
            adminUser.setLibrarianId("ADMIN-001");
            adminUser.setEmail("admin@library.com");

            // 3. Save the new admin user to the database
            userRepository.save(adminUser);
            
            System.out.println("*****************************************************");
            System.out.println("ADMIN USER CREATED: username='admin', password='admin'");
            System.out.println("*****************************************************");
        } else {
             System.out.println("Admin user 'admin' already exists.");
        }
    }
}