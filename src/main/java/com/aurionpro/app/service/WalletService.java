package com.aurionpro.app.service;

import com.aurionpro.app.dto.RechargeRequest;
import com.aurionpro.app.dto.WalletDto;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import java.math.BigDecimal;

public interface WalletService {
    // Returns a DTO with transaction history
    WalletDto getWalletDetailsForUser(User user);
    Wallet getWalletByUser(User user); // Internal helper
    void debit(Wallet wallet, BigDecimal amount); // Internal helper
    WalletDto credit(Wallet wallet, RechargeRequest rechargeRequest); // Returns DTO
}