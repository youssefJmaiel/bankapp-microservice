package com.bankapp.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private Long id;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private String ownerId;
    private BigDecimal amount;
    private String currency;
    private String reference;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
