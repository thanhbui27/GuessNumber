package com.guessgame.controller;

import com.guessgame.dto.leaderboard.LeaderboardResponse;
import com.guessgame.service.LeaderboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    LeaderboardResponse leaderboard() {
        return leaderboardService.top10();
    }
}
