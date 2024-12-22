package com.example.csticaret.service.user;

import com.example.csticaret.dto.UserDto;
import com.example.csticaret.exceptions.AlreadyExistsException;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.exceptions.ValidationException;
import com.example.csticaret.model.User;
import com.example.csticaret.repository.UserRepository;
import com.example.csticaret.service.jwt.JwtService;
import com.example.csticaret.request.CreateUserRequest;
import com.example.csticaret.request.SignInRequest;
import com.example.csticaret.request.UserUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    public User createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new AlreadyExistsException("Email already registered");
        }
        
        try {
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            
            User savedUser = userRepository.save(user);
            log.info("User created successfully with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            log.error("Error creating user: ", e);
            throw e;
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    @Override
    public User updateUser(UserUpdateRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AlreadyExistsException("Email already taken");
            }
            if (!isValidEmail(request.getEmail())) {
                throw new ValidationException("Invalid email format");
            }
            user.setEmail(request.getEmail());
        }
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    @Override
    public UserDto convertUserToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        return userDto;
    }

    @Override
    public User signIn(SignInRequest request) {
        log.info("Attempting sign in for email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.error("User not found with email: {}", request.getEmail());
                return new ResourceNotFoundException("User not found with email: " + request.getEmail());
            });
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Invalid credentials for user: {}", request.getEmail());
            throw new ResourceNotFoundException("Invalid credentials");
        }
        
        String token = jwtService.generateToken(user);
        user.setToken(token);
        
        log.info("Sign in successful for user: {}", request.getEmail());
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
} 