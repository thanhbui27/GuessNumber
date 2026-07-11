package com.guessgame.mapper;

import com.guessgame.dto.game.GuessHistoryResponse;
import com.guessgame.dto.game.PurchaseHistoryResponse;
import com.guessgame.entity.GuessHistory;
import com.guessgame.entity.PurchaseHistory;
import org.springframework.stereotype.Component;

@Component
public class HistoryMapper {
    public GuessHistoryResponse toGuessHistoryResponse(GuessHistory history) {
        return new GuessHistoryResponse(
                history.getId(),
                history.getGuessedNumber(),
                history.getServerNumber(),
                history.getResult(),
                history.getScoreAfter(),
                history.getTurnsAfter(),
                history.getCreatedAt()
        );
    }

    public PurchaseHistoryResponse toPurchaseHistoryResponse(PurchaseHistory history) {
        return new PurchaseHistoryResponse(
                history.getId(),
                history.getTurnsAdded(),
                history.getAmount(),
                history.getProvider(),
                history.getTransactionCode(),
                history.getStatus(),
                history.getCreatedAt()
        );
    }
}
