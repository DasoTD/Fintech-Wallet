package com.example.fintechwallet.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    private Long receiverId;
    private BigDecimal amount;
}