package com.guessgame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.guessgame.config.GameProperties;
import com.guessgame.dto.game.GuessRequest;
import com.guessgame.dto.game.GuessResponse;
import com.guessgame.entity.GuessHistory;
import com.guessgame.entity.User;
import com.guessgame.enums.GuessResult;
import com.guessgame.enums.Role;
import com.guessgame.exception.ApiException;
import com.guessgame.mapper.HistoryMapper;
import com.guessgame.repository.GuessHistoryRepository;
import com.guessgame.repository.PurchaseHistoryRepository;
import com.guessgame.repository.UserRepository;
import com.guessgame.service.GameOutcome;
import com.guessgame.service.GameOutcomeGenerator;
import com.guessgame.service.GameService;
import com.guessgame.service.PaymentGatewayRegistry;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    GuessHistoryRepository guessHistoryRepository;
    @Mock
    PurchaseHistoryRepository purchaseHistoryRepository;
    @Mock
    GameOutcomeGenerator outcomeGenerator;
    @Mock
    PaymentGatewayRegistry paymentGatewayRegistry;
    @Mock
    GameProperties gameProperties;
    @Mock
    HistoryMapper historyMapper;

    @InjectMocks
    GameService gameService;

    @Test
    void guessWinDeductsTurnAndAddsScore() {
        User user = new User("thanh", "thanh@example.com", "hash", 5, Role.USER);
        user.setId(1L);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(outcomeGenerator.generate(3)).thenReturn(new GameOutcome(3, GuessResult.WIN));
        when(guessHistoryRepository.save(any(GuessHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GuessResponse response = gameService.guess(1L, new GuessRequest(3));

        assertThat(response.correct()).isTrue();
        assertThat(response.score()).isEqualTo(1);
        assertThat(response.remainingTurns()).isEqualTo(4);
        verify(guessHistoryRepository).save(any(GuessHistory.class));
    }

    @Test
    void guessWithoutTurnsDoesNotWriteHistory() {
        User user = new User("thanh", "thanh@example.com", "hash", 0, Role.USER);
        user.setId(1L);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> gameService.guess(1L, new GuessRequest(2)))
                .isInstanceOf(ApiException.class)
                .hasMessage("Ban da het luot choi.");

        verify(guessHistoryRepository, never()).save(any());
    }
}
