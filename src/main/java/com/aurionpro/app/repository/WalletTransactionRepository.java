package com.aurionpro.app.repository;

import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Integer> {
    List<WalletTransaction> findByWalletOrderByTransactionTimeDesc(Wallet wallet);
}