package com.aurionpro.app.service.implementation;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.entity.RefreshToken;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.repository.RefreshTokenRepository;
import com.aurionpro.app.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${application.security.jwt.refresh-token.expiration-ms}")
    private long refreshTokenDurationMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Find if a token already exists for the user
        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByUser(user);

        RefreshToken refreshToken;
        if (existingTokenOpt.isPresent()) {
            // If a token exists, update its value and expiry date
            refreshToken = existingTokenOpt.get();
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        } else {
            // If no token exists, create a new one
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                    .build();
        }
        
        // Save the updated or new token
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidOperationException("Refresh token not found. Please log in again."));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidOperationException("Refresh token has expired. Please log in again.");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void deleteRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}