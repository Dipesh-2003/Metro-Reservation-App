package com.aurionpro.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurionpro.app.entity.RefreshToken;
import com.aurionpro.app.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);
    
    void deleteByUser(User user);
}