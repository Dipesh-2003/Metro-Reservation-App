package com.aurionpro.app.dto;


import com.aurionpro.app.common.TransactionType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class WalletTransactionDto {
    private Integer transactionId;
    private BigDecimal amount;
    private TransactionType type;
    private Instant transactionTime;
}
