package com.example.csticaret.Controller.User;



import com.example.csticaret.dto.UserDto;
import com.example.csticaret.exceptions.AlreadyExistsException;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.User;
import com.example.csticaret.repository.UserRepository;
import com.example.csticaret.request.CreateUserRequest;
import com.example.csticaret.request.SignInRequest;
import com.example.csticaret.request.UserUpdateRequest;
import com.example.csticaret.service.jwt.JwtService;
import com.example.csticaret.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        User result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(mockUser.getId(), result.getId());
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_AlreadyExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> userService.createUser(request));
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("UpdatedFirstName");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setFirstName("OldFirstName");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.updateUser(request, userId);

        // Assert
        assertNotNull(result);
        assertEquals("UpdatedFirstName", result.getFirstName());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testSignIn_Success() {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.getPassword(), mockUser.getPassword())).thenReturn(true);

        // Act
        User result = userService.signIn(request);

        // Assert
        assertNotNull(result);
        assertEquals(mockUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), mockUser.getPassword());
    }

    @Test
    void testSignIn_InvalidPassword() {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.getPassword(), mockUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.signIn(request));
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), mockUser.getPassword());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Arrange
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("UpdatedFirstName");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(request, userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

}

