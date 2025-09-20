package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RechargeRequest {
    private BigDecimal amount;
}
