package com.guessgame.dto.user;

import com.guessgame.enums.Role;
import java.time.LocalDateTime;

public record CurrentUserResponse(
        Long id,
        String username,
        String email,
        Integer score,
        Integer turns,
        long rank,
        Role role,
        LocalDateTime createdAt
) {
}
