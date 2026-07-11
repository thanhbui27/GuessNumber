package com.guessgame.dto.game;

import com.guessgame.enums.GuessResult;
import java.time.LocalDateTime;

public record GuessResponse(
        int guessedNumber,
        int serverNumber,
        boolean correct,
        GuessResult result,
        String message,
        int score,
        int remainingTurns,
        LocalDateTime playedAt
) {
}
