package com.aurionpro.app.repository;

import com.aurionpro.app.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Integer> {
    Optional<OtpVerification> findByEmailAndOtpCode(String email, String otpCode);
}