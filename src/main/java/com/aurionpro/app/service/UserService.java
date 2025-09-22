package com.aurionpro.app.service;

import com.aurionpro.app.dto.SignUpRequest;
import com.aurionpro.app.entity.User;

public interface UserService {
    User registerUser(SignUpRequest signUpRequest);
    User findByEmail(String email);
}