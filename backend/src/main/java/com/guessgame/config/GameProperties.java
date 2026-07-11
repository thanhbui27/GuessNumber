package com.guessgame.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "game")
public class GameProperties {
    @Min(1)
    private int defaultTurns = 5;
    @Min(1)
    private int buyTurnsAmount = 5;
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double winRate = 0.05;
    @Min(1)
    private int minNumber = 1;
    @Min(1)
    private int maxNumber = 5;

    public int getDefaultTurns() {
        return defaultTurns;
    }

    public void setDefaultTurns(int defaultTurns) {
        this.defaultTurns = defaultTurns;
    }

    public int getBuyTurnsAmount() {
        return buyTurnsAmount;
    }

    public void setBuyTurnsAmount(int buyTurnsAmount) {
        this.buyTurnsAmount = buyTurnsAmount;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public int getMinNumber() {
        return minNumber;
    }

    public void setMinNumber(int minNumber) {
        this.minNumber = minNumber;
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }
}
