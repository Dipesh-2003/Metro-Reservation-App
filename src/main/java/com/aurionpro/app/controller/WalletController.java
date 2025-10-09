package com.aurionpro.app.controller;

import com.aurionpro.app.dto.RechargeRequest;
import com.aurionpro.app.dto.WalletDto;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.service.UserService;
import com.aurionpro.app.service.WalletService;
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
@SecurityRequirement(name = "bearerAuth") //security to all endpoints in this controller
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

    @PostMapping("/recharge")
    @Operation(summary = "Recharge user's wallet", description = "Adds funds to the authenticated user's wallet. This is a mock payment for now.")
    public ResponseEntity<WalletDto> rechargeWallet(@RequestBody RechargeRequest rechargeRequest, Principal principal) {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        WalletDto updatedWallet = walletService.rechargeWallet(currentUser, rechargeRequest);
        return ResponseEntity.ok(updatedWallet);
    }
}