// package com.example.fintechwallet.service;

// import com.example.fintechwallet.entity.User;
// import com.example.fintechwallet.repository.UserRepository;
// import com.stripe.exception.StripeException;
// import com.stripe.model.ExternalAccount;
// import com.stripe.model.PaymentIntent;
// import com.stripe.model.Payout;
// import com.stripe.param.PayoutCreateParams;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.math.BigDecimal;
// import java.util.HashMap;
// import java.util.Map;

// @Service
// public class StripeService {

//     @Autowired
//     private UserRepository userRepository;

//     public String createPaymentIntent(Long userId, BigDecimal amountInDollars) throws StripeException {
//         User user = userRepository.findById(userId).orElseThrow();

//         Map<String, Object> params = new HashMap<>();
//         params.put("amount", amountInDollars.multiply(BigDecimal.valueOf(100)).longValue());
//         params.put("currency", "usd");
//         params.put("metadata", Map.of("userId", userId.toString()));

//         PaymentIntent intent = PaymentIntent.create(params);
//         return intent.getClientSecret();
//     }

//     @Transactional
//     public void handleSuccessfulFunding(PaymentIntent paymentIntent) {
//         String userIdStr = paymentIntent.getMetadata().get("userId");
//         if (userIdStr == null) return;

//         Long userId = Long.parseLong(userIdStr);
//         User user = userRepository.findById(userId).orElseThrow();

//         BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmountReceived()).divide(BigDecimal.valueOf(100));
//         user.setBalance(user.getBalance().add(amount));
//         userRepository.save(user);
//     }

//     public String addBankAccount(Long userId, String bankToken) throws StripeException {
//         User user = userRepository.findById(userId).orElseThrow();

//         // Assume stripeCustomerId is set (add creation logic if needed)
//         com.stripe.model.Customer stripeCustomer = com.stripe.model.Customer.retrieve(user.getStripeCustomerId());

//         Map<String, Object> params = new HashMap<>();
//         params.put("external_account", bankToken);
//         ExternalAccount bank = stripeCustomer.getSources().create(params);

//         Map<String, Object> defaultParams = new HashMap<>();
//         defaultParams.put("default_for_currency", bank.getId());
//         stripeCustomer.update(defaultParams);

//         user.setStripeBankAccountId(bank.getId());
//         userRepository.save(user);

//         return bank.getId();
//     }

//     @Transactional
//     public Payout createPayout(Long userId, BigDecimal amountInDollars) throws StripeException {
//         User user = userRepository.findById(userId).orElseThrow();

//         if (user.getBalance().compareTo(amountInDollars) < 0) {
//             throw new RuntimeException("Insufficient balance");
//         }

//         if (user.getStripeBankAccountId() == null) {
//             throw new RuntimeException("No bank account linked");
//         }

//         long amountInCents = amountInDollars.multiply(BigDecimal.valueOf(100)).longValueExact();

//         PayoutCreateParams params = PayoutCreateParams.builder()
//                 .setAmount(amountInCents)
//                 .setCurrency("usd")
//                 .putMetadata("userId", userId.toString())
//                 .build();

//         Payout payout = Payout.create(params);

//         if ("pending".equals(payout.getStatus()) || "in_transit".equals(payout.getStatus())) {
//             user.setBalance(user.getBalance().subtract(amountInDollars));
//             userRepository.save(user);
//         }

//         return payout;
//     }
// }