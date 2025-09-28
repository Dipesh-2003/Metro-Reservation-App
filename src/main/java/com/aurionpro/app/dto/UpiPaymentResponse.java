package com.aurionpro.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpiPaymentResponse {
    private String razorpayOrderId;
    private Integer paymentId;
    private BigDecimal amount;
    private String currency;
    private String apiKey;
}