package com.example.csticaret.Controller.User;




import com.example.csticaret.controller.UserController;
import com.example.csticaret.dto.UserDto;
import com.example.csticaret.exceptions.AlreadyExistsException;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.User;
import com.example.csticaret.request.CreateUserRequest;
import com.example.csticaret.request.SignInRequest;
import com.example.csticaret.request.UserUpdateRequest;
import com.example.csticaret.response.ApiResponse;
import com.example.csticaret.response.AuthenticationResponse;
import com.example.csticaret.service.jwt.JwtService;
import com.example.csticaret.service.user.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private IUserService userService;

    @Mock
    private JwtService jwtService;

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
        UserDto mockUserDto = new UserDto();
        mockUserDto.setId(userId);

        when(userService.getUserById(userId)).thenReturn(mockUser);
        when(userService.convertUserToDto(mockUser)).thenReturn(mockUserDto);

        // Act
        ResponseEntity<ApiResponse> response = userController.getUserById(userId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(mockUserDto, response.getBody().getData());
        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).convertUserToDto(mockUser);
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        Long userId = 1L;
        when(userService.getUserById(userId)).thenThrow(new ResourceNotFoundException("User not found!"));

        // Act
        ResponseEntity<ApiResponse> response = userController.getUserById(userId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found!", response.getBody().getMessage());
        verify(userService, times(1)).getUserById(userId);
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

        String mockToken = "mockToken";

        when(userService.createUser(request)).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser)).thenReturn(mockToken);
        when(userService.convertUserToDto(mockUser)).thenReturn(new UserDto(mockUser.getId(), mockUser.getEmail(), mockUser.getFirstName(), mockUser.getLastName()));

        // Act
        ResponseEntity<ApiResponse> response = userController.createUser(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User created successfully", response.getBody().getMessage());
        verify(userService, times(1)).createUser(request);
        verify(jwtService, times(1)).generateToken(mockUser);
    }

    @Test
    void testCreateUser_AlreadyExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");

        when(userService.createUser(request)).thenThrow(new AlreadyExistsException("Email already exists"));

        // Act
        ResponseEntity<ApiResponse> response = userController.createUser(request);

        // Assert
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Email already exists", response.getBody().getMessage());
        verify(userService, times(1)).createUser(request);
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("UpdatedFirstName");

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setFirstName("UpdatedFirstName");

        UserDto mockUserDto = new UserDto(mockUser.getId(), mockUser.getEmail(), mockUser.getFirstName(), mockUser.getLastName());

        when(userService.updateUser(request, userId)).thenReturn(mockUser);
        when(userService.convertUserToDto(mockUser)).thenReturn(mockUserDto);

        // Act
        ResponseEntity<ApiResponse> response = userController.updateUser(request, userId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Update User Success!", response.getBody().getMessage());
        assertEquals(mockUserDto, response.getBody().getData());
        verify(userService, times(1)).updateUser(request, userId);
        verify(userService, times(1)).convertUserToDto(mockUser);
    }

    @Test
    void testSignIn_Success() {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User mockUser = new User();
        mockUser.setEmail(request.getEmail());

        String mockToken = "mockToken";

        UserDto mockUserDto = new UserDto(mockUser.getId(), mockUser.getEmail(), mockUser.getFirstName(), mockUser.getLastName());

        when(userService.signIn(request)).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser)).thenReturn(mockToken);
        when(userService.convertUserToDto(mockUser)).thenReturn(mockUserDto);

        // Act
        ResponseEntity<ApiResponse> response = userController.signIn(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Sign in successful!", response.getBody().getMessage());
        verify(userService, times(1)).signIn(request);
        verify(jwtService, times(1)).generateToken(mockUser);
    }

    @Test
    void testSignIn_UserNotFound() {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userService.signIn(request)).thenThrow(new ResourceNotFoundException("User not found"));

        // Act
        ResponseEntity<ApiResponse> response = userController.signIn(request);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody().getMessage());
        verify(userService, times(1)).signIn(request);
    }
}

