package com.bankapp.payment.converter;

import com.bankapp.payment.domain.PaymentDto;
import com.bankapp.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentDtoConverter {

    public PaymentDto convert(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentDto.builder()
                .id(payment.getId())
                .sourceAccountId(payment.getSourceAccountId())
                .destinationAccountId(payment.getDestinationAccountId())
                .ownerId(payment.getOwnerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .reference(payment.getReference())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
