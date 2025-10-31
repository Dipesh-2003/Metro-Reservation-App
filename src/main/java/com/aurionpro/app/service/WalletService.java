package com.aurionpro.app.service;

import com.aurionpro.app.dto.RechargeRequest;
import com.aurionpro.app.dto.WalletDto;
import com.aurionpro.app.dto.WalletRechargeResponse;
import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.razorpay.RazorpayException;

import java.math.BigDecimal;

public interface WalletService {
    
    WalletDto getWalletDetailsForUser(User user);

    Wallet getWalletByUser(User user); 

    void debit(Wallet wallet, BigDecimal amount); 

    WalletDto credit(Wallet wallet, RechargeRequest rechargeRequest);
    
    WalletDto rechargeWallet(User user, RechargeRequest rechargeRequest);
    
    WalletRechargeResponse initiateWalletRecharge(User user, RechargeRequest rechargeRequest) throws RazorpayException;
    
    void creditWalletFromPayment(Payment payment);
}