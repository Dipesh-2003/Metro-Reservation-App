package com.aurionpro.app.service.implementation;

import com.aurionpro.app.common.PaymentMethod;
import com.aurionpro.app.common.PaymentStatus;
import com.aurionpro.app.common.TransactionType;
import com.aurionpro.app.dto.RechargeRequest;
import com.aurionpro.app.dto.WalletDto;
import com.aurionpro.app.dto.WalletRechargeResponse;
import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.entity.WalletTransaction;
import com.aurionpro.app.exception.InsufficientFundsException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.UserMapper;
import com.aurionpro.app.repository.PaymentRepository;
import com.aurionpro.app.repository.WalletRepository;
import com.aurionpro.app.repository.WalletTransactionRepository;
import com.aurionpro.app.service.UserService;
import com.aurionpro.app.service.WalletService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor 
public class WalletServiceImpl implements WalletService {

		private final WalletRepository walletRepository;
	    private final WalletTransactionRepository transactionRepository;
	    private final PaymentRepository paymentRepository;
	    private final UserMapper userMapper;
	    private final UserService userService; 
	    private final RazorpayClient razorpayClient; 
	    
	    @Value("${razorpay.api.key}")
	    private String apiKey;

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

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEBIT);
        transaction.setTransactionTime(Instant.now());
        transactionRepository.save(transaction);
    }
    
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

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(savedWallet);
        transaction.setAmount(rechargeRequest.getAmount());
        transaction.setType(TransactionType.CREDIT);
        transaction.setTransactionTime(Instant.now());
        transactionRepository.save(transaction);
        
        return getWalletDetailsForUser(wallet.getUser());
    }

    @Override
    @Transactional
    public WalletRechargeResponse initiateWalletRecharge(User user, RechargeRequest rechargeRequest) throws RazorpayException {
        Payment payment = new Payment();
        payment.setAmount(rechargeRequest.getAmount());
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(Instant.now());
        payment.setUser(user);
        Payment savedPayment = paymentRepository.save(payment);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", rechargeRequest.getAmount().multiply(new BigDecimal(100)).intValue());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "receipt_wallet_" + savedPayment.getPaymentId());

        JSONObject notes = new JSONObject();
        notes.put("paymentId", savedPayment.getPaymentId().toString());
        notes.put("userId", user.getUserId().toString());
        notes.put("type", "WALLET_RECHARGE");
        orderRequest.put("notes", notes);
        
        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        savedPayment.setTransactionId(razorpayOrderId);
        paymentRepository.save(savedPayment);

        return new WalletRechargeResponse(razorpayOrderId, savedPayment.getPaymentId(), rechargeRequest.getAmount(), "INR", apiKey);
    }

    @Override
    @Transactional
    public void creditWalletFromPayment(Payment payment) {
        Wallet wallet = getWalletByUser(payment.getUser());
        wallet.setBalance(wallet.getBalance().add(payment.getAmount()));
        wallet.setLastUpdated(Instant.now());
        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(savedWallet);
        transaction.setAmount(payment.getAmount());
        transaction.setType(TransactionType.CREDIT);
        transaction.setTransactionTime(Instant.now());
        transaction.setPayment(payment);
        transactionRepository.save(transaction);
    }
}