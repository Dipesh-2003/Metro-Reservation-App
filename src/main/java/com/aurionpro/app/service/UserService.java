package com.aurionpro.app.service;

import com.aurionpro.app.dto.ResetPasswordRequest;
import com.aurionpro.app.dto.SignUpRequest;
import com.aurionpro.app.dto.UserDto;
import com.aurionpro.app.entity.User;

public interface UserService {
    UserDto registerUser(SignUpRequest signUpRequest);
    UserDto findByEmail(String email);
    
    User findUserEntityByEmail(String email);
    
    User findUserEntityById(Integer userId);
    UserDto updateUserProfileImage(String email, String imageUrl);
    
    void deleteUserAccount(String email, String password);
    
    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequest request);


}