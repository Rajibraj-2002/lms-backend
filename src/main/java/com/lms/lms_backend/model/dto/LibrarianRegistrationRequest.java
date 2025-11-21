package com.lms.lms_backend.model.dto;

import lombok.Data;

@Data // Lombok annotation to create getters, setters, etc.
public class LibrarianRegistrationRequest {
    private String authorizationKey;
    private String name;
    private String librarianId;
    private String email;
    private String username;
    private String password;
}