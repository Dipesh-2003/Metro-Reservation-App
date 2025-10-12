package com.aurionpro.app.controller;

import com.aurionpro.app.dto.RechargeRequest;
import com.aurionpro.app.dto.WalletDto;
import com.aurionpro.app.dto.WalletRechargeResponse;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.service.UserService;
import com.aurionpro.app.service.WalletService;
import com.razorpay.RazorpayException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "4. Wallet Management", description = "APIs for managing user wallets")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user's wallet details", description = "Retrieves the wallet balance and transaction history for the authenticated user.")
    public ResponseEntity<WalletDto> getMyWallet(Principal principal) {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        WalletDto walletDetails = walletService.getWalletDetailsForUser(currentUser);
        return ResponseEntity.ok(walletDetails);
    }

    @PostMapping("/recharge/initiate")
    @Operation(summary = "Initiate a wallet recharge using Razorpay", description = "Creates a Razorpay order for recharging the user's wallet.")
    public ResponseEntity<WalletRechargeResponse> initiateWalletRecharge(@RequestBody RechargeRequest rechargeRequest, Principal principal) throws RazorpayException {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        WalletRechargeResponse response = walletService.initiateWalletRecharge(currentUser, rechargeRequest);
        return ResponseEntity.ok(response);
    }
}