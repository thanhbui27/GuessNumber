package com.guessgame.service;

import com.guessgame.config.GameProperties;
import com.guessgame.dto.common.PageResponse;
import com.guessgame.dto.game.BuyTurnsResponse;
import com.guessgame.dto.game.GuessHistoryResponse;
import com.guessgame.dto.game.GuessRequest;
import com.guessgame.dto.game.GuessResponse;
import com.guessgame.dto.game.PurchaseHistoryResponse;
import com.guessgame.entity.GuessHistory;
import com.guessgame.entity.PurchaseHistory;
import com.guessgame.entity.User;
import com.guessgame.enums.PaymentProvider;
import com.guessgame.enums.PaymentStatus;
import com.guessgame.exception.ApiException;
import com.guessgame.mapper.HistoryMapper;
import com.guessgame.repository.GuessHistoryRepository;
import com.guessgame.repository.PurchaseHistoryRepository;
import com.guessgame.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameService {
    private final UserRepository userRepository;
    private final GuessHistoryRepository guessHistoryRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final GameOutcomeGenerator outcomeGenerator;
    private final PaymentGatewayRegistry paymentGatewayRegistry;
    private final GameProperties gameProperties;
    private final HistoryMapper historyMapper;

    public GameService(UserRepository userRepository,
                       GuessHistoryRepository guessHistoryRepository,
                       PurchaseHistoryRepository purchaseHistoryRepository,
                       GameOutcomeGenerator outcomeGenerator,
                       PaymentGatewayRegistry paymentGatewayRegistry,
                       GameProperties gameProperties,
                       HistoryMapper historyMapper) {
        this.userRepository = userRepository;
        this.guessHistoryRepository = guessHistoryRepository;
        this.purchaseHistoryRepository = purchaseHistoryRepository;
        this.outcomeGenerator = outcomeGenerator;
        this.paymentGatewayRegistry = paymentGatewayRegistry;
        this.gameProperties = gameProperties;
        this.historyMapper = historyMapper;
    }

    @Transactional
    public GuessResponse guess(Long userId, GuessRequest request) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Khong tim thay user."));
        if (user.getTurns() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ban da het luot choi.");
        }

        GameOutcome outcome = outcomeGenerator.generate(request.number());
        user.setTurns(user.getTurns() - 1);
        if (outcome.isWin()) {
            user.setScore(user.getScore() + 1);
        }

        GuessHistory history = guessHistoryRepository.save(new GuessHistory(
                user,
                request.number(),
                outcome.serverNumber(),
                outcome.result(),
                user.getScore(),
                user.getTurns()
        ));

        String message = outcome.isWin()
                ? "Chuc mung! Ban da doan dung."
                : "Chua chinh xac. Hay thu lai.";
        return new GuessResponse(
                request.number(),
                outcome.serverNumber(),
                outcome.isWin(),
                outcome.result(),
                message,
                user.getScore(),
                user.getTurns(),
                history.getCreatedAt() == null ? LocalDateTime.now() : history.getCreatedAt()
        );
    }

    @Transactional
    public BuyTurnsResponse buyTurns(Long userId, PaymentProvider provider, String ipAddress) {
        int addedTurns = gameProperties.getBuyTurnsAmount();
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Khong tim thay user."));
        PaymentProvider selectedProvider = provider == null ? PaymentProvider.DEMO : provider;
        PaymentService paymentService = paymentGatewayRegistry.require(selectedProvider);
        PaymentStartResult payment = paymentService.startPayment(userId, gameProperties.getBuyTurnsPrice(), addedTurns, ipAddress);

        if (payment.status() != PaymentStatus.SUCCESS) {
            purchaseHistoryRepository.save(new PurchaseHistory(user, 0, payment.amount(), payment.provider(), payment.transactionCode(), payment.status()));
            return new BuyTurnsResponse("Vui long hoan tat thanh toan.", 0, user.getTurns(), payment.transactionCode(), payment.paymentUrl());
        }

        user.setTurns(user.getTurns() + addedTurns);
        purchaseHistoryRepository.save(new PurchaseHistory(user, addedTurns, payment.amount(), payment.provider(), payment.transactionCode(), payment.status()));
        return new BuyTurnsResponse("Mua them luot choi thanh cong.", addedTurns, user.getTurns(), payment.transactionCode(), payment.paymentUrl());
    }

    @Transactional(readOnly = true)
    public PageResponse<GuessHistoryResponse> getGuessHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<GuessHistoryResponse> result = guessHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(historyMapper::toGuessHistoryResponse);
        return toPageResponse(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseHistoryResponse> getPurchaseHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PurchaseHistoryResponse> result = purchaseHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(historyMapper::toPurchaseHistoryResponse);
        return toPageResponse(result);
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}
