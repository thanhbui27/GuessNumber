package com.guessgame.dto.user;

import com.guessgame.enums.Role;

public record UserResponse(
        Long id,
        String username,
        String email,
        Integer score,
        Integer turns,
        Role role
) {
}
