// In: com/aurionpro/app/service/implementation/WalletServiceImpl.java
package com.aurionpro.app.service.implementation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aurionpro.app.dto.RechargeRequest;
// ... (imports)
import com.aurionpro.app.dto.WalletDto;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.entity.WalletTransaction;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.UserMapper;
import com.aurionpro.app.repository.WalletRepository;
import com.aurionpro.app.repository.WalletTransactionRepository;
import com.aurionpro.app.service.WalletService;

import jakarta.transaction.Transactional;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletTransactionRepository transactionRepository;
    @Autowired
    private UserMapper userMapper; // <-- Inject mapper

    @Override
    public WalletDto getWalletDetailsForUser(User user) {
        Wallet wallet = getWalletByUser(user);
        
        // Fetch the transactions for this wallet
        List<WalletTransaction> transactions = transactionRepository.findByWalletOrderByTransactionTimeDesc(wallet);
        
        // Map the wallet entity to a DTO
        WalletDto walletDto = userMapper.entityToDto(wallet);
        
        // Map the transactions and set them on the DTO
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
        // ... (debit logic is perfect, no changes needed)
    }

    @Override
    @Transactional
    public WalletDto credit(Wallet wallet, RechargeRequest rechargeRequest) {
        // ... (credit logic is perfect, but we need to return the updated wallet)
        wallet.setBalance(wallet.getBalance().add(rechargeRequest.getAmount()));
        wallet.setLastUpdated(Instant.now());
        Wallet savedWallet = walletRepository.save(wallet);

        // ... (transaction logging)
        
        // Return the DTO of the updated wallet
        return userMapper.entityToDto(savedWallet);
    }
}