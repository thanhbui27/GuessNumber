package com.guessgame.service;

import com.guessgame.enums.PaymentProvider;
import com.guessgame.enums.PaymentStatus;
import java.math.BigDecimal;

public record PaymentStartResult(
        PaymentProvider provider,
        PaymentStatus status,
        BigDecimal amount,
        String transactionCode,
        String paymentUrl
) {
}
