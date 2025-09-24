package com.aurionpro.app.service.implementation;

import com.aurionpro.app.common.Role;
import com.aurionpro.app.dto.JwtResponse;
import com.aurionpro.app.dto.VerifyOtpRequestDto;
import com.aurionpro.app.entity.OtpVerification;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.repository.OtpVerificationRepository;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.repository.WalletRepository;
import com.aurionpro.app.security.JwtService;
import com.aurionpro.app.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final JavaMailSender mailSender;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void sendOtp(String email) {
        //generate a 6-digit OTP
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        //expiry time(5 minutes from now)
        Instant expiryTime = Instant.now().plus(5, ChronoUnit.MINUTES);

        // Create and save the OTP entity
        OtpVerification otp = new OtpVerification();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setExpiryTimestamp(expiryTime);
        otp.setIsVerified(false);
        otpRepository.save(otp);

        // Send the email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@metroapp.com");
        message.setTo(email);
        message.setSubject("Your Metro App Verification Code");
        message.setText("Your OTP for Metro App is: " + otpCode + ". It is valid for 5 minutes.");
        mailSender.send(message);
    }

    @Override
    @Transactional
    public JwtResponse verifyOtp(VerifyOtpRequestDto verifyRequest) {
        // Find the OTP by email and code
        OtpVerification otp = otpRepository.findByEmailAndOtpCode(verifyRequest.getEmail(), verifyRequest.getOtpCode())
                .orElseThrow(() -> new InvalidOperationException("Invalid OTP code provided."));

        // Check if the OTP is already verified or expired
        if (otp.getIsVerified()) {
            throw new InvalidOperationException("This OTP has already been used.");
        }
        if (otp.getExpiryTimestamp().isBefore(Instant.now())) {
            throw new InvalidOperationException("OTP has expired. Please request a new one.");
        }

        // Mark the OTP as verified
        otp.setIsVerified(true);
        otpRepository.save(otp);

        // Find or create the user
        User user = userRepository.findByEmail(verifyRequest.getEmail())
                .orElseGet(() -> createNewUser(verifyRequest.getEmail()));

        // Generate JWT token
        final UserDetails userDetails = user;
        final String jwtToken = jwtService.generateToken(userDetails);

        return new JwtResponse(jwtToken);
    }

    private User createNewUser(String email) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName("User"); // A default name
        newUser.setRole(Role.USER);
        // This user has no password, as they log in via OTP
        User savedUser = userRepository.save(newUser);

        //create a wallet for the new user
        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setLastUpdated(Instant.now());
        walletRepository.save(wallet);

        return savedUser;
    }
}