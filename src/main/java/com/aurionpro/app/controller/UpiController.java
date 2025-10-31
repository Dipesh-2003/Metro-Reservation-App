package com.aurionpro.app.controller;

import com.aurionpro.app.dto.UpiPaymentRequest;
import com.aurionpro.app.dto.UpiPaymentResponse;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.service.PaymentService;
import com.aurionpro.app.service.UserService;
import com.razorpay.RazorpayException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "7. UPI Payments", description = "APIs for handling UPI payments via Razorpay")
public class UpiController {

    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/upi/pay")
    @Operation(summary = "Initiate a UPI payment", description = "Creates a Razorpay order and returns details for the client to process the payment.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UpiPaymentResponse> createUpiPayment(@RequestBody UpiPaymentRequest request, Principal principal) throws RazorpayException {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        UpiPaymentResponse response = paymentService.createUpiOrder(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payments/webhook/razorpay")
    @Operation(summary = "Razorpay webhook handler", description = "Handles payment confirmation callbacks from Razorpay. This endpoint is public.")
    // CHANGE THE REQUEST BODY TYPE FROM Map<String, Object> TO String
    public ResponseEntity<Void> handleRazorpayWebhook(@RequestBody String payload, @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}