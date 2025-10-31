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
                userRepository.delete(existingUser);
            }
        });

    User user = new User();
    user.setName(signUpRequest.getName());
    user.setEmail(signUpRequest.getEmail());
    user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
    user.setRole(Role.USER);
    user.setEnabled(false); 
    
    User savedUser = userRepository.save(user);

    Wallet wallet = new Wallet();
    wallet.setUser(savedUser);
    wallet.setBalance(BigDecimal.ZERO);
    wallet.setLastUpdated(Instant.now());
    walletRepository.save(wallet);

    otpService.sendOtp(savedUser.getEmail());

    return userMapper.entityToDto(savedUser); 
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
        User user = findUserEntityByEmail(email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Incorrect password. Account deletion failed.");
        }

        refreshTokenRepository.deleteByUser(user);

        walletRepository.findByUser(user).ifPresent(wallet -> {
            walletTransactionRepository.deleteAll(walletTransactionRepository.findByWalletOrderByTransactionTimeDesc(wallet));
            walletRepository.delete(wallet);
        });
        
        List<Ticket> tickets = ticketRepository.findByUserOrderByBookingTimeDesc(user);
        if (!tickets.isEmpty()) {
        }

        ticketRepository.deleteAll(tickets);
        
        List<Payment> payments = paymentRepository.findByUser(user);
        paymentRepository.deleteAll(payments);

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = findUserEntityByEmail(email); 

        otpService.sendOtp(email);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        OtpVerification otp = otpRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new InvalidOperationException("Invalid OTP code provided."));

        if (otp.getIsVerified()) {
            throw new InvalidOperationException("This OTP has already been used.");
        }
        if (otp.getExpiryTimestamp().isBefore(Instant.now())) {
            throw new InvalidOperationException("OTP has expired. Please request a new one.");
        }

        User user = findUserEntityByEmail(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otp.setIsVerified(true);
        otpRepository.save(otp);
    }
}