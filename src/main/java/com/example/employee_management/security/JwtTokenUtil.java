package com.example.employee_management.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil {

    // Replace this with property reading if you want (application.properties)
    private static final String SECRET_KEY = "thisIsASecretKeyForJwtSigningThatShouldBeLongEnough";
    private static final long EXPIRATION_TIME = 86400000L; // 1 day

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Generate token using username + role
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Overload: generate from Spring Authentication
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String role = "EMPLOYEE";
        if (authentication.getAuthorities() != null && authentication.getAuthorities().iterator().hasNext()) {
            role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        }
        return generateToken(username, role);
    }

    public String getUsernameFromJwt(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public String getRoleFromJwt(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
