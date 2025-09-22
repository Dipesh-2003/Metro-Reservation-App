package com.aurionpro.app.service.implementation;

import com.aurionpro.app.common.TransactionType;
import com.aurionpro.app.dto.RechargeRequest;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.entity.WalletTransaction;
import com.aurionpro.app.exception.InsufficientFundsException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.repository.WalletRepository;
import com.aurionpro.app.repository.WalletTransactionRepository;
import com.aurionpro.app.service.WalletService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    @Override
    public Wallet getWalletByUser(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + user.getEmail()));
    }

    @Override
    @Transactional
    public void debit(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Not enough balance in wallet");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setLastUpdated(Instant.now());
        walletRepository.save(wallet);

        // Log the transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEBIT);
        transaction.setTransactionTime(Instant.now());
        transactionRepository.save(transaction);
    }
    
    @Override
    @Transactional
    public void credit(Wallet wallet, RechargeRequest rechargeRequest) {
        wallet.setBalance(wallet.getBalance().add(rechargeRequest.getAmount()));
        wallet.setLastUpdated(Instant.now());
        walletRepository.save(wallet);

        //log the transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(rechargeRequest.getAmount());
        transaction.setType(TransactionType.CREDIT);
        transaction.setTransactionTime(Instant.now());
        transactionRepository.save(transaction);
    }
}