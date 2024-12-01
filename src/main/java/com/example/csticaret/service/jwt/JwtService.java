package com.example.csticaret.service.jwt;

import com.example.csticaret.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUsername(String token);
    String generateToken(User user);
    boolean isTokenValid(String token, UserDetails userDetails);
} 