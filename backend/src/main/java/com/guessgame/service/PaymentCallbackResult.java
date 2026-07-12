package com.guessgame.service;

import com.guessgame.enums.PaymentStatus;

public record PaymentCallbackResult(
        PaymentStatus status,
        String transactionCode,
        String rspCode,
        String message
) {
}
