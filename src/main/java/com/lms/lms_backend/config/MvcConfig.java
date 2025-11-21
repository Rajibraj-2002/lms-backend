package com.lms.lms_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull; // 1. ADD THIS IMPORT
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // This is the only variable we need
    @Value("${upload.dir}")
    private String uploadDir;

    // 2. The 'coverImageUrl' variable was unused and has been DELETED.

    @Override
    // 3. ADD THE @NonNull ANNOTATION HERE
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // This maps the URL /uploads/... to your physical D:/Project/LMS-Uploads/ folder
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}