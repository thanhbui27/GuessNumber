package com.guessgame.service;

import com.guessgame.dto.leaderboard.LeaderboardItem;
import com.guessgame.dto.leaderboard.LeaderboardResponse;
import com.guessgame.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaderboardService {
    private final UserRepository userRepository;

    public LeaderboardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse top10() {
        List<LeaderboardItem> items = new ArrayList<>();
        List<com.guessgame.entity.User> users = userRepository.findTop10ByOrderByScoreDescCreatedAtAscIdAsc();
        for (int i = 0; i < users.size(); i++) {
            com.guessgame.entity.User user = users.get(i);
            items.add(new LeaderboardItem(i + 1L, user.getId(), user.getUsername(), user.getScore(), user.getTurns()));
        }
        return new LeaderboardResponse(items);
    }
}
