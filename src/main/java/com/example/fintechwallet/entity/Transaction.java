package com.example.fintechwallet.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User sender;
    @ManyToOne
    private User receiver;
    private BigDecimal amount;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String status;  // SUCCESS, FAILED
}