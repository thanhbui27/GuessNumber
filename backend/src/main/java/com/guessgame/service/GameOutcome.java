package com.guessgame.service;

import com.guessgame.enums.GuessResult;

public record GameOutcome(int serverNumber, GuessResult result) {
    public boolean isWin() {
        return result == GuessResult.WIN;
    }
}
