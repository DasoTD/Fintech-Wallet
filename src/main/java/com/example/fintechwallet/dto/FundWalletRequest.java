package com.example.fintechwallet.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FundWalletRequest {
    private BigDecimal amount;
    private String currency = "usd";
}