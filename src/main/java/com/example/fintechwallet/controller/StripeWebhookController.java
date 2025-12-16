package com.example.fintechwallet.controller;

import com.example.fintechwallet.config.StripeConfig;
import com.example.fintechwallet.entity.User;
import com.example.fintechwallet.repository.UserRepository;
import com.example.fintechwallet.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Payout;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class StripeWebhookController {
    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    @Autowired
    private StripeConfig stripeConfig;
    @Autowired
    private StripeService stripeService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/api/stripe/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.warn("Webhook payload invalid: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
            stripeService.handleSuccessfulFunding(paymentIntent);
            log.info("Wallet funded via PaymentIntent {}", paymentIntent.getId());
        } else if ("payout.failed".equals(event.getType())) {
            Payout payout = (Payout) event.getDataObjectDeserializer().getObject().get();
            String userIdStr = payout.getMetadata().get("userId");
            if (userIdStr != null) {
                Long userId = Long.parseLong(userIdStr);
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    BigDecimal amount = BigDecimal.valueOf(payout.getAmount()).divide(BigDecimal.valueOf(100));
                    user.setBalance(user.getBalance().add(amount));
                    userRepository.save(user);
                    log.info("Payout failed; balance restored for user {}", userId);
                }
            }
        }

        return ResponseEntity.ok("Webhook received");
    }
}