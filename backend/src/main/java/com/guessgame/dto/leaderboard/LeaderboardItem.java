package com.guessgame.dto.leaderboard;

public record LeaderboardItem(
        long rank,
        Long userId,
        String username,
        int score,
        int turns
) {
}
