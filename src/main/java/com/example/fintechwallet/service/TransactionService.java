package com.example.fintechwallet.service;

import com.example.fintechwallet.entity.Transaction;
import com.example.fintechwallet.entity.User;
import com.example.fintechwallet.repository.TransactionRepository;
import com.example.fintechwallet.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @SuppressWarnings("null")
    @Transactional
    public Transaction transfer(Long senderId, Long receiverId, BigDecimal amount) {
        log.info("Starting transfer: senderId={}, receiverId={}, amount={}", senderId, receiverId, amount);

        try {
            User sender = userRepository.findById(senderId).orElseThrow();
            User receiver = userRepository.findById(receiverId).orElseThrow();

            if (sender.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient balance");
            }

            sender.setBalance(sender.getBalance().subtract(amount));
            receiver.setBalance(receiver.getBalance().add(amount));

            userRepository.save(sender);
            userRepository.save(receiver);

            Transaction tx = new Transaction();
            tx.setSender(sender);
            tx.setReceiver(receiver);
            tx.setAmount(amount);
            tx.setStatus("SUCCESS");

            log.info("Transfer successful: senderId={}, receiverId={}, amount={}", senderId, receiverId, amount);
            return transactionRepository.save(tx);
        } catch (Exception e) {
            log.error("Transfer failed: senderId={}, receiverId={}, amount={}", senderId, receiverId, amount, e);
            throw e;
        }
    }
}