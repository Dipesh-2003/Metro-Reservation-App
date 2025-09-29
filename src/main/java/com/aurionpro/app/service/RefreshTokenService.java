package com.aurionpro.app.service;

import com.aurionpro.app.entity.RefreshToken;
import com.aurionpro.app.entity.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    RefreshToken verifyRefreshToken(String token);
    void deleteRefreshToken(User user);
}