package com.lms.lms_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lms.lms_backend.model.dto.AuthRequest;
import com.lms.lms_backend.model.dto.AuthResponse;
import com.lms.lms_backend.model.dto.LibrarianRegistrationRequest;
import com.lms.lms_backend.model.dto.ProfileResponse;
import com.lms.lms_backend.model.dto.ResetPasswordRequest;
import com.lms.lms_backend.model.entity.User;
import com.lms.lms_backend.service.AuthService;
import com.lms.lms_backend.service.CustomUserDetailsService;
import com.lms.lms_backend.service.UserService;
import com.lms.lms_backend.util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final UserService userService; // 2. INJECT UserService

    public AuthController(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil, AuthService authService, UserService userService) { // 3. ADD to constructor
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.userService = userService; // 4. ASSIGN it
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            final String jwt = jwtUtil.generateToken(userDetails);
            
            // Extract the role from UserDetails
            String role = userDetails.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User has no roles"))
                .getAuthority()
                .replace("ROLE_", ""); // "ROLE_LIBRARIAN" -> "LIBRARIAN"

            return ResponseEntity.ok(new AuthResponse(jwt, role));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @PostMapping("/register/librarian")
    public ResponseEntity<?> registerLibrarian(@RequestBody LibrarianRegistrationRequest request) {
        try {
            User librarian = authService.registerLibrarian(request);
            return ResponseEntity.ok(librarian);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 5. ADD THIS NEW ENDPOINT ---
    /**
     * GET http://localhost:8080/api/auth/my-profile
     * Gets the profile for ANY logged-in user (Admin or User)
     */
    @GetMapping("/my-profile")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        // We re-use the logic from UserService
        return ResponseEntity.ok(userService.getUserProfile(userDetails.getUsername()));
    }
    //reset password for librarian
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetLibrarianPassword(request.getUsername(), request.getAdminKey(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}