package com.aurionpro.app.service;

import com.aurionpro.app.dto.SignUpRequest;
import com.aurionpro.app.dto.UserDto;
import com.aurionpro.app.entity.User;

public interface UserService {
    UserDto registerUser(SignUpRequest signUpRequest);
    UserDto findByEmail(String email);
    
    User findUserEntityByEmail(String email);
}