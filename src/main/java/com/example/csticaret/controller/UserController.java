package com.example.csticaret.controller;

import com.example.csticaret.dto.UserDto;
import com.example.csticaret.exceptions.AlreadyExistsException;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.User;
import com.example.csticaret.request.CreateUserRequest;
import com.example.csticaret.request.SignInRequest;
import com.example.csticaret.request.UserUpdateRequest;
import com.example.csticaret.response.ApiResponse;
import com.example.csticaret.response.AuthenticationResponse;
import com.example.csticaret.service.user.IUserService;
import com.example.csticaret.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {
    private final IUserService userService;
    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/{userId}/user")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            UserDto userDto = userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("Success", userDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request);
            String token = jwtService.generateToken(user);
            UserDto userDto = userService.convertUserToDto(user);
            
            AuthenticationResponse response = AuthenticationResponse.builder()
                .token(token)
                .user(userDto)
                .build();
            
            return ResponseEntity.ok(new ApiResponse("User created successfully", response));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }
    @PutMapping("/{userId}/update")
    public ResponseEntity<ApiResponse> updateUser(@RequestBody UserUpdateRequest request, @PathVariable Long userId) {
        try {
            User user = userService.updateUser(request, userId);
            UserDto userDto = userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("Update User Success!", userDto));
        } catch (ResourceNotFoundException e) {
           return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new ApiResponse("Delete User Success!", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse> signIn(@RequestBody SignInRequest request) {
        try {
            log.info("Sign in attempt for email: {}", request.getEmail());
            User user = userService.signIn(request);
            String token = jwtService.generateToken(user);
            UserDto userDto = userService.convertUserToDto(user);
            
            AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .token(token)
                .user(userDto)
                .build();
            
            log.info("Sign in successful for user: {}", user.getEmail());
            return ResponseEntity.ok(new ApiResponse("Sign in successful!", authResponse));
        } catch (ResourceNotFoundException e) {
            log.error("Sign in failed: {}", e.getMessage());
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

     
}
