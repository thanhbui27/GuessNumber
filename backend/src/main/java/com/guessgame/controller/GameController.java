package com.guessgame.controller;

import com.guessgame.dto.common.PageResponse;
import com.guessgame.dto.game.BuyTurnsResponse;
import com.guessgame.dto.game.BuyTurnsRequest;
import com.guessgame.dto.game.GuessHistoryResponse;
import com.guessgame.dto.game.GuessRequest;
import com.guessgame.dto.game.GuessResponse;
import com.guessgame.dto.game.PurchaseHistoryResponse;
import com.guessgame.security.UserPrincipal;
import com.guessgame.service.GameService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/game")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/guess")
    GuessResponse guess(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody GuessRequest request) {
        return gameService.guess(principal.getId(), request);
    }

    @PostMapping("/buy-turns")
    BuyTurnsResponse buyTurns(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) BuyTurnsRequest buyTurnsRequest,
            HttpServletRequest request
    ) {
        return gameService.buyTurns(principal.getId(), buyTurnsRequest == null ? null : buyTurnsRequest.provider(), clientIp(request));
    }

    @GetMapping("/history")
    PageResponse<GuessHistoryResponse> history(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        return gameService.getGuessHistory(principal.getId(), page, size);
    }

    @GetMapping("/purchase-history")
    PageResponse<PurchaseHistoryResponse> purchaseHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        return gameService.getPurchaseHistory(principal.getId(), page, size);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
