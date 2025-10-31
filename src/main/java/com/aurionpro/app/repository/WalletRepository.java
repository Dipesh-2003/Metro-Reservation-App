package com.aurionpro.app.repository;

import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    Optional<Wallet> findByUser(User user);
}