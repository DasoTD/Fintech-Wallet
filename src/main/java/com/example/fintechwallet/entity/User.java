package com.example.fintechwallet.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;  // Hashed
    private String email;
    private BigDecimal balance = BigDecimal.ZERO;
    private String stripeCustomerId;  // Optional for Stripe
    private String stripeBankAccountId;  // For payouts
}