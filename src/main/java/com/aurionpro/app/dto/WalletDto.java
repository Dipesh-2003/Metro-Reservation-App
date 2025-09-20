package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data 
public class WalletDto {
    private Integer walletId;
    private BigDecimal balance;
    private List<WalletTransactionDto> transactions;
}
