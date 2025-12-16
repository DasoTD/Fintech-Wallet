package com.example.fintechwallet.repository;

import com.example.fintechwallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}