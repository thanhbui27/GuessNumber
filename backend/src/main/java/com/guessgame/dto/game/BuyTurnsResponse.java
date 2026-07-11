package com.guessgame.dto.game;

public record BuyTurnsResponse(
        String message,
        int addedTurns,
        int currentTurns,
        String transactionCode
) {
}
