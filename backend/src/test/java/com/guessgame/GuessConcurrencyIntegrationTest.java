package com.guessgame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.guessgame.dto.game.GuessRequest;
import com.guessgame.entity.User;
import com.guessgame.enums.GuessResult;
import com.guessgame.enums.Role;
import com.guessgame.exception.ApiException;
import com.guessgame.repository.GuessHistoryRepository;
import com.guessgame.repository.UserRepository;
import com.guessgame.service.GameOutcome;
import com.guessgame.service.GameOutcomeGenerator;
import com.guessgame.service.GameService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GuessConcurrencyIntegrationTest {
    @Autowired
    GameService gameService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    GuessHistoryRepository guessHistoryRepository;
    @MockBean
    GameOutcomeGenerator outcomeGenerator;

    @Test
    void concurrentGuessesDoNotMakeTurnsNegative() throws Exception {
        when(outcomeGenerator.generate(anyInt())).thenReturn(new GameOutcome(2, GuessResult.LOSE));
        User user = userRepository.save(new User("concurrent_user", "concurrent@example.com", "hash", 5, Role.USER));

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(() -> {
                try {
                    gameService.guess(user.getId(), new GuessRequest(1));
                    return true;
                } catch (ApiException ex) {
                    return false;
                }
            });
        }

        long successCount = executor.invokeAll(tasks).stream()
                .filter(future -> {
                    try {
                        return future.get();
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .count();
        executor.shutdown();

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        assertThat(successCount).isEqualTo(5);
        assertThat(reloaded.getTurns()).isZero();
        assertThat(reloaded.getTurns()).isNotNegative();
        assertThat(guessHistoryRepository.countByUserId(user.getId())).isEqualTo(5);
    }
}
