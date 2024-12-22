package com.example.csticaret.service;

import com.example.csticaret.dto.UserDto;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.User;
import com.example.csticaret.repository.UserRepository;
import com.example.csticaret.request.SignInRequest;
import com.example.csticaret.request.SignUpRequest;
import com.example.csticaret.response.AuthenticationResponse;
import com.example.csticaret.service.jwt.JwtService;
import com.example.csticaret.service.cart.ICartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ICartService cartService;
    private final HttpSession httpSession;

    @Override
    public AuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user);
        UserDto userDto = convertToUserDto(user);

        return AuthenticationResponse.builder()
            .token(token)
            .user(userDto)
            .build();
    }

    @Override
    @Transactional
    public AuthenticationResponse signUp(SignUpRequest request) {
        User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

        user = userRepository.save(user);

        // Transfer guest cart if exists
        String guestId = (String) httpSession.getAttribute("GUEST_ID");
        if (guestId != null) {
            cartService.transferGuestCartToUser(guestId, user);
            httpSession.removeAttribute("GUEST_ID");
        }

        String token = jwtService.generateToken(user);
        UserDto userDto = convertToUserDto(user);

        return AuthenticationResponse.builder()
            .token(token)
            .user(userDto)
            .build();
    }

    private UserDto convertToUserDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .build();
    }
} 