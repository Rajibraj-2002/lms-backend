package com.lms.lms_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ensure the path ends with a slash
        String path = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        
        // Use file:/// for absolute paths on Linux/Unix systems like Render
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + path);
    }
}