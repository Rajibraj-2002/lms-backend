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
        
        // Map the URL "/uploads/**" to the physical file location "file:/tmp/"
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }
}