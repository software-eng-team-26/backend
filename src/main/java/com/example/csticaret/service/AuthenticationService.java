package com.example.csticaret.service;

import com.example.csticaret.request.SignInRequest;
import com.example.csticaret.request.SignUpRequest;
import com.example.csticaret.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse signIn(SignInRequest request);
    AuthenticationResponse signUp(SignUpRequest request);
} 