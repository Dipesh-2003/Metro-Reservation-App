package com.aurionpro.app.service;

import com.aurionpro.app.dto.RechargeRequest;
import com.aurionpro.app.dto.WalletDto;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import java.math.BigDecimal;

public interface WalletService {
    
    //returns DTO with transaction history
    WalletDto getWalletDetailsForUser(User user);

    Wallet getWalletByUser(User user); 

    void debit(Wallet wallet, BigDecimal amount); 

    //this method is for internal use, so we will create a new one for the controller
    WalletDto credit(Wallet wallet, RechargeRequest rechargeRequest);
    
    WalletDto rechargeWallet(User user, RechargeRequest rechargeRequest);
}