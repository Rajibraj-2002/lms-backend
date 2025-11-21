package com.lms.lms_backend.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey; // 1. IMPORT THIS

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims; // 2. IMPORT THIS
import io.jsonwebtoken.Jwts; // 3. IMPORT THIS
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours

    public JwtUtil(@Value("${jwt.secret.key}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 4. THIS METHOD IS UPDATED
    public String generateToken(UserDetails userDetails) {
        // --- Create a "claims" map to add extra info (like the role) ---
        Map<String, Object> claims = new HashMap<>();
        
        // Get the first role (e.g., "ROLE_LIBRARIAN") and strip the "ROLE_" prefix
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER") // Default just in case
                .replace("ROLE_", ""); // We just want "LIBRARIAN" or "USER"

        claims.put("role", role);
        // -------------------------------------------------------------

        return Jwts.builder()
                .claims(claims) // Add the custom claims map
                .subject(userDetails.getUsername()) 
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    // 5. THIS METHOD IS NEW (To extract the role)
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    // ... (rest of the file is unchanged) ...

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}