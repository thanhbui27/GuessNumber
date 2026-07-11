package com.guessgame.dto.leaderboard;

import java.util.List;

public record LeaderboardResponse(List<LeaderboardItem> items) {
}
