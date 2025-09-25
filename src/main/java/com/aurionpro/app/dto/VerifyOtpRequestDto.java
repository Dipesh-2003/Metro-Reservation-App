package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class VerifyOtpRequestDto {
    private String email;
    private String otpCode;
}