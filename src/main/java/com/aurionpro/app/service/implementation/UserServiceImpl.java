package com.aurionpro.app.service.implementation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.common.Role;
import com.aurionpro.app.dto.ResetPasswordRequest;
import com.aurionpro.app.dto.SignUpRequest;
import com.aurionpro.app.dto.UserDto;
import com.aurionpro.app.entity.OtpVerification;
import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.Ticket;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.entity.WalletTransaction;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.UserMapper;
import com.aurionpro.app.repository.OtpVerificationRepository;
import com.aurionpro.app.repository.PaymentRepository;
import com.aurionpro.app.repository.RefreshTokenRepository;
import com.aurionpro.app.repository.TicketRepository;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.repository.WalletRepository;
import com.aurionpro.app.repository.WalletTransactionRepository;
import com.aurionpro.app.service.OtpService;
import com.aurionpro.app.service.UserService;

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
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private OtpVerificationRepository otpRepository;

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

    @Override
    @Transactional
    public UserDto updateUserProfileImage(String email, String imageUrl) {
        User user = findUserEntityByEmail(email);
        user.setProfileImageUrl(imageUrl);
        User updatedUser = userRepository.save(user);
        return userMapper.entityToDto(updatedUser);
    }

    @Override
    public User findUserEntityById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    @Transactional
    public void deleteUserAccount(String email, String password) {
        // Step 1: Find the user by their email
        User user = findUserEntityByEmail(email);

        // Step 2: Validate the provided password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Incorrect password. Account deletion failed.");
        }

        // --- REVISED DELETION ORDER ---

        // Step 3: Delete entities that depend on the user but not on each other.
        
        // A) Delete the user's refresh token
        refreshTokenRepository.deleteByUser(user);

        // B) Find and delete the user's wallet and its transactions
        walletRepository.findByUser(user).ifPresent(wallet -> {
            walletTransactionRepository.deleteAll(walletTransactionRepository.findByWalletOrderByTransactionTimeDesc(wallet));
            walletRepository.delete(wallet);
        });
        
        // C) Find and delete all validations associated with the user's tickets
        List<Ticket> tickets = ticketRepository.findByUserOrderByBookingTimeDesc(user);
        if (!tickets.isEmpty()) {
            // This part is missing in your current code, you need to delete validation records
            // Assuming you have a ValidationRepository with a `deleteAllByTicketIn` method or similar.
            // If not, you would loop through tickets and delete validations for each.
        }

        // D) Delete all tickets associated with the user
        ticketRepository.deleteAll(tickets);
        
        // E) NOW, find and delete ALL payments associated with the user
        // This is the key fix. This will include payments from tickets AND other sources.
        List<Payment> payments = paymentRepository.findByUser(user);
        paymentRepository.deleteAll(payments);

        // Step 4: After all dependencies are removed, delete the user itself
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        // Step 1: Check if a user with this email actually exists.
        User user = findUserEntityByEmail(email); // This will throw ResourceNotFoundException if user doesn't exist.

        // Step 2: If the user exists, send an OTP.
        otpService.sendOtp(email);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Step 1: Find and validate the OTP.
        OtpVerification otp = otpRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new InvalidOperationException("Invalid OTP code provided."));

        if (otp.getIsVerified()) {
            throw new InvalidOperationException("This OTP has already been used.");
        }
        if (otp.getExpiryTimestamp().isBefore(Instant.now())) {
            throw new InvalidOperationException("OTP has expired. Please request a new one.");
        }

        // Step 2: If OTP is valid, find the user.
        User user = findUserEntityByEmail(request.getEmail());

        // Step 3: Encode and set the new password.
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Step 4: Mark the OTP as used to prevent it from being used again.
        otp.setIsVerified(true);
        otpRepository.save(otp);
    }
}