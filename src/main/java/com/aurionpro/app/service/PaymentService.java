package com.aurionpro.app.service;

import com.aurionpro.app.dto.UpiPaymentRequest;
import com.aurionpro.app.dto.UpiPaymentResponse;
import com.aurionpro.app.entity.User;
import com.razorpay.RazorpayException;
import java.util.Map;

public interface PaymentService {
    UpiPaymentResponse createUpiOrder(UpiPaymentRequest request, User user) throws RazorpayException;
    
 // CHANGE THE PAYLOAD TYPE HERE AS WELL
    void handleWebhook(String payload, String signature);
}