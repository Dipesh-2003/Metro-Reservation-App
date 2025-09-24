package com.aurionpro.app.service;

import com.aurionpro.app.dto.JwtResponse;
import com.aurionpro.app.dto.VerifyOtpRequestDto;

public interface OtpService {
    void sendOtp(String email);
    JwtResponse verifyOtp(VerifyOtpRequestDto verifyRequest);
}