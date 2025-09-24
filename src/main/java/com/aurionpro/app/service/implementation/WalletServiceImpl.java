package com.aurionpro.app.service.implementation;

import com.aurionpro.app.common.TransactionType;
import com.aurionpro.app.dto.RechargeRequest;
import com.aurionpro.app.dto.WalletDto;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.entity.WalletTransaction;
import com.aurionpro.app.exception.InsufficientFundsException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.UserMapper;
import com.aurionpro.app.repository.WalletRepository;
import com.aurionpro.app.repository.WalletTransactionRepository;
import com.aurionpro.app.service.UserService;
import com.aurionpro.app.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor // Use Lombok for constructor injection
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final UserMapper userMapper;
    private final UserService userService; // Assuming you have a UserService

    @Override
    public WalletDto getWalletDetailsForUser(User user) {
        Wallet wallet = getWalletByUser(user);
        List<WalletTransaction> transactions = transactionRepository.findByWalletOrderByTransactionTimeDesc(wallet);
        
        WalletDto walletDto = userMapper.entityToDto(wallet);
        walletDto.setTransactions(userMapper.entityToDto(transactions));
        
        return walletDto;
    }

    @Override
    public Wallet getWalletByUser(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + user.getEmail()));
    }

    @Override
    @Transactional
    public void debit(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in wallet. Current balance: " + wallet.getBalance());
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setLastUpdated(Instant.now());
        walletRepository.save(wallet);

        // Record the transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEBIT);
        transaction.setTransactionTime(Instant.now());
        transactionRepository.save(transaction);
    }
    
    // --- NEW IMPLEMENTATION ---
    @Override
    @Transactional
    public WalletDto rechargeWallet(User user, RechargeRequest rechargeRequest) {
        Wallet wallet = getWalletByUser(user);
        return credit(wallet, rechargeRequest);
    }
    
    @Override
    @Transactional
    public WalletDto credit(Wallet wallet, RechargeRequest rechargeRequest) {
        wallet.setBalance(wallet.getBalance().add(rechargeRequest.getAmount()));
        wallet.setLastUpdated(Instant.now());
        Wallet savedWallet = walletRepository.save(wallet);

        // Record the transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(savedWallet);
        transaction.setAmount(rechargeRequest.getAmount());
        transaction.setType(TransactionType.CREDIT);
        transaction.setTransactionTime(Instant.now());
        transactionRepository.save(transaction);
        
        return getWalletDetailsForUser(wallet.getUser());
    }
}