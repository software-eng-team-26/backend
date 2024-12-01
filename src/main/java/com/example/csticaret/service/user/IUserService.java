package com.example.csticaret.service.user;

import com.example.csticaret.dto.UserDto;
import com.example.csticaret.model.User;
import com.example.csticaret.request.CreateUserRequest;
import com.example.csticaret.request.SignInRequest;
import com.example.csticaret.request.UserUpdateRequest;

public interface IUserService {

    User getUserById(Long id);
    User getUserByEmail(String email);
    User createUser(CreateUserRequest request);
    User updateUser(UserUpdateRequest request, Long id);
    void deleteUser(Long id);
    User signIn(SignInRequest request);
    UserDto convertUserToDto(User user);
}
