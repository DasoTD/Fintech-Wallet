package com.example.fintechwallet.controller;

import com.example.fintechwallet.config.StripeConfig;
import com.example.fintechwallet.dto.*;
import com.example.fintechwallet.entity.Transaction;
import com.example.fintechwallet.entity.User;
import com.example.fintechwallet.service.StripeService;
import com.example.fintechwallet.service.TransactionService;
import com.example.fintechwallet.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Payout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    @Autowired
    private UserService userService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private StripeService stripeService;
    @Autowired
    private StripeConfig stripeConfig;

    @GetMapping("/balance")
    public BigDecimal getBalance(@AuthenticationPrincipal User user) {
        return user.getBalance();
    }

    @PostMapping("/transfer")
    public Transaction transfer(@AuthenticationPrincipal User user, @RequestBody TransferRequest request) {
        return transactionService.transfer(user.getId(), request.getReceiverId(), request.getAmount());
    }

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return Map.of("publishableKey", stripeConfig.publishableKey);
    }

    @PostMapping("/fund")
    public Map<String, String> fundWallet(@AuthenticationPrincipal User user, @RequestBody FundWalletRequest request) {
        try {
            String clientSecret = stripeService.createPaymentIntent(user.getId(), request.getAmount());
            return Map.of("clientSecret", clientSecret);
        } catch (StripeException e) {
            throw new RuntimeException("Payment intent failed: " + e.getMessage());
        }
    }

    @PostMapping("/add-bank")
    public Map<String, String> addBank(@AuthenticationPrincipal User user, @RequestBody AddBankRequest request) {
        try {
            String bankId = stripeService.addBankAccount(user.getId(), request.getBankToken());
            return Map.of("bankId", bankId, "message", "Bank added successfully");
        } catch (StripeException e) {
            throw new RuntimeException("Bank add failed: " + e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public Map<String, Object> withdraw(@AuthenticationPrincipal User user, @RequestBody WithdrawRequest request) {
        try {
            Payout payout = stripeService.createPayout(user.getId(), request.getAmount());
            return Map.of(
                    "payoutId", payout.getId(),
                    "status", payout.getStatus(),
                    "arrivalDate", payout.getArrivalDate(),
                    "message", "Withdrawal initiated"
            );
        } catch (StripeException e) {
            throw new RuntimeException("Withdrawal failed: " + e.getMessage());
        }
    }
}