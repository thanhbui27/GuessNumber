package com.guessgame;

import static org.assertj.core.api.Assertions.assertThat;

import com.guessgame.entity.User;
import com.guessgame.enums.Role;
import com.guessgame.security.JwtService;
import com.guessgame.security.UserPrincipal;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    @Test
    void generatesAndValidatesTokenWithPlainTextSecret() {
        JwtService jwtService = new JwtService("plain-text-secret-key-for-hs256-long-enough", 86400000);
        User user = new User("alice", "alice@example.com", "hash", 5, Role.USER);
        user.setId(1L);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.isValid(token, UserPrincipal.from(user))).isTrue();
    }
}
