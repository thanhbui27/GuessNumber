package com.guessgame.dto.game;

import com.guessgame.enums.GuessResult;
import java.time.LocalDateTime;

public record GuessHistoryResponse(
        Long id,
        int guessedNumber,
        int serverNumber,
        GuessResult result,
        int scoreAfter,
        int turnsAfter,
        LocalDateTime createdAt
) {
}
