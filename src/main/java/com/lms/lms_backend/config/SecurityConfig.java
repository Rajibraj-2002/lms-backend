// package com.lms.lms_backend.config;

// import java.util.List;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration; 
// import org.springframework.http.HttpMethod;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.web.cors.CorsConfiguration;

// import com.lms.lms_backend.model.enums.Role;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

//     private final JwtAuthFilter jwtAuthFilter;
    
//     public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
//         this.jwtAuthFilter = jwtAuthFilter;
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//             .csrf(csrf -> csrf.disable())
//             .cors(cors -> cors.configurationSource(request -> {
//                 CorsConfiguration config = new CorsConfiguration();
//                 config.setAllowedOrigins(List.of(
//                     "http://localhost:5173",
//                     "http://127.0.0.1:5173",
//                     "https://lms-bhadrak.vercel.app/" // <-- VERCEL URL
//                 ));
//                 config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//                 config.setAllowedHeaders(List.of("*"));
//                 config.setAllowCredentials(true);
//                 return config;
//             }))
//             .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
//             .authorizeHttpRequests(auth -> auth
//                 // Allow all OPTIONS requests (CORS preflight)
//                 .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
            
//                 // --- FIX: Explicitly allow Login, Register, and Reset Password ---
//                 .requestMatchers("/api/auth/login", "/api/auth/register/librarian", "/api/auth/reset-password").permitAll() 
                
//                 // Allow Public Resources & WebSockets
//                 .requestMatchers("/api/public/**", "/uploads/**", "/ws/**").permitAll() 
                
//                 // Protected Routes
//                 .requestMatchers("/api/admin/**").hasRole(Role.LIBRARIAN.name())
//                 .requestMatchers("/api/user/**").hasRole(Role.USER.name())
                
//                 // Any other request (like /api/auth/my-profile) requires authentication
//                 .anyRequest().authenticated()
//             )
            
//             .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

//         return http.build();
//     }
    
//     @Bean
//     public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//          return config.getAuthenticationManager();
//     }
// }


//update WebSocket connection is being blocked for the vercel deployment
package com.lms.lms_backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; 
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.lms.lms_backend.model.enums.Role;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                
                // --- FIX: Allow ANY Origin (Nuclear Option) ---
                // This solves all 403 CORS issues immediately
                config.setAllowedOriginPatterns(List.of("*")); 
                
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
            
                // Public Endpoints
                .requestMatchers("/api/auth/**", "/api/public/**", "/uploads/**", "/ws/**").permitAll() 
                
                // Protected Routes
                .requestMatchers("/api/admin/**").hasRole(Role.LIBRARIAN.name())
                .requestMatchers("/api/user/**").hasRole(Role.USER.name())
                .anyRequest().authenticated()
            )
            
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
         return config.getAuthenticationManager();
    }
}