package com.aurionpro.app.service;

import com.aurionpro.app.dto.SignUpRequest;
import com.aurionpro.app.dto.UserDto;
import com.aurionpro.app.entity.User;

public interface UserService {
    // Return DTOs instead of entities
    UserDto registerUser(SignUpRequest signUpRequest);
    UserDto findByEmail(String email);
    
    // This is a helper method for internal use by the security layer, so returning the entity is ok.
    User findUserEntityByEmail(String email);
}