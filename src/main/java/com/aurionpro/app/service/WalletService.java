package com.aurionpro.app.service;

import com.aurionpro.app.dto.RechargeRequest;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import java.math.BigDecimal;

public interface WalletService {
    Wallet getWalletByUser(User user);
    void debit(Wallet wallet, BigDecimal amount);
    void credit(Wallet wallet, RechargeRequest rechargeRequest);
}