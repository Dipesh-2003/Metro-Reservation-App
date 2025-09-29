package com.aurionpro.app.service.implementation;

import com.aurionpro.app.entity.RefreshToken;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.repository.RefreshTokenRepository;
import com.aurionpro.app.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${application.security.jwt.refresh-token.expiration-ms}")
    private long refreshTokenDurationMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // First, delete any existing refresh token for this user
        refreshTokenRepository.deleteByUser(user);

        // Create a new refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

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