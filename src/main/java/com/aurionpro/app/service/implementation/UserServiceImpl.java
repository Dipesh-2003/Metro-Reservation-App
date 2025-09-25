package com.aurionpro.app.service.implementation;

import com.aurionpro.app.common.Role;
import com.aurionpro.app.dto.SignUpRequest;
import com.aurionpro.app.dto.UserDto;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.UserMapper;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.repository.WalletRepository;
import com.aurionpro.app.service.OtpService;
import com.aurionpro.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OtpService otpService;

    @Override
    @Transactional
    public UserDto registerUser(SignUpRequest signUpRequest) {
    	userRepository.findByEmail(signUpRequest.getEmail())
        .ifPresent(existingUser -> {
            if (existingUser.isEnabled()) {
                throw new InvalidOperationException("User with this email already exists.");
            } else {
                // If the user exists but is not enabled, delete them to allow a fresh registration attempt
                userRepository.delete(existingUser);
            }
        });

    User user = new User();
    user.setName(signUpRequest.getName());
    user.setEmail(signUpRequest.getEmail());
    user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
    user.setRole(Role.USER);
    user.setEnabled(false); // <-- Set user as disabled by default
    
    User savedUser = userRepository.save(user);

    // Wallet creation logic remains the same
    Wallet wallet = new Wallet();
    wallet.setUser(savedUser);
    wallet.setBalance(BigDecimal.ZERO);
    wallet.setLastUpdated(Instant.now());
    walletRepository.save(wallet);

    // Send OTP for verification
    otpService.sendOtp(savedUser.getEmail());

    return userMapper.entityToDto(savedUser); // You can still return the DTO or change to void/String
}

    @Override
    public UserDto findByEmail(String email) {
        User user = findUserEntityByEmail(email);
        return userMapper.entityToDto(user);
    }

    @Override
    public User findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}