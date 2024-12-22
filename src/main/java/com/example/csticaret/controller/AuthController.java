package com.example.csticaret.controller;

import com.example.csticaret.request.SignInRequest;
import com.example.csticaret.request.SignUpRequest;
import com.example.csticaret.response.ApiResponse;
import com.example.csticaret.response.AuthenticationResponse;
import com.example.csticaret.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse> signIn(@RequestBody SignInRequest request) {
        try {
            AuthenticationResponse response = authenticationService.signIn(request);
            return ResponseEntity.ok(new ApiResponse("Successfully signed in", response));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse("Invalid email or password", null));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signUp(@RequestBody SignUpRequest request) {
        AuthenticationResponse response = authenticationService.signUp(request);
        return ResponseEntity.ok(new ApiResponse("Successfully signed up", response));
    }
} 