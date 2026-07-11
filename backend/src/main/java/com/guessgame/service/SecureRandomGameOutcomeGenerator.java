package com.guessgame.service;

import com.guessgame.config.GameProperties;
import com.guessgame.enums.GuessResult;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SecureRandomGameOutcomeGenerator implements GameOutcomeGenerator {
    private final GameProperties gameProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public SecureRandomGameOutcomeGenerator(GameProperties gameProperties) {
        this.gameProperties = gameProperties;
    }

    @Override
    public GameOutcome generate(int guessedNumber) {
        boolean isWin = secureRandom.nextDouble() < gameProperties.getWinRate();
        if (isWin) {
            return new GameOutcome(guessedNumber, GuessResult.WIN);
        }
        List<Integer> candidates = new ArrayList<>();
        for (int number = gameProperties.getMinNumber(); number <= gameProperties.getMaxNumber(); number++) {
            if (number != guessedNumber) {
                candidates.add(number);
            }
        }
        int serverNumber = candidates.get(secureRandom.nextInt(candidates.size()));
        return new GameOutcome(serverNumber, GuessResult.LOSE);
    }
}
