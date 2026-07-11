package com.guessgame.dto.game;

import com.guessgame.enums.PaymentProvider;
import com.guessgame.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseHistoryResponse(
        Long id,
        int turnsAdded,
        BigDecimal amount,
        PaymentProvider provider,
        String transactionCode,
        PaymentStatus status,
        LocalDateTime createdAt
) {
}
