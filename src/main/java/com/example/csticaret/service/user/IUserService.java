package com.example.csticaret.service.user;

import com.example.csticaret.dto.UserDto;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.User;
import com.example.csticaret.request.CreateUserRequest;
import com.example.csticaret.request.UserUpdateRequest;
import com.example.csticaret.request.SignInRequest;

public interface IUserService {

    User getUserById(Long userId);
    User createUser(CreateUserRequest request);
    User updateUser(UserUpdateRequest request, Long userId);
    void deleteUser(Long userId);
    User signIn(SignInRequest request) throws ResourceNotFoundException;
    UserDto convertUserToDto(User user);
}
