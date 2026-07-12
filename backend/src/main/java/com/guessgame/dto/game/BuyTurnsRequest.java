package com.guessgame.dto.game;

import com.guessgame.enums.PaymentProvider;

public record BuyTurnsRequest(
        PaymentProvider provider
) {
}
