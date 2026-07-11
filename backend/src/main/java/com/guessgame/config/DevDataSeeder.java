package com.guessgame.config;

import com.guessgame.entity.User;
import com.guessgame.enums.Role;
import com.guessgame.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DevDataSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GameProperties gameProperties;

    public DevDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, GameProperties gameProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.gameProperties = gameProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String[] names = {"alice", "bob", "charlie", "david", "eva", "frank", "grace", "henry", "ivy", "jack"};
        for (int i = 0; i < names.length; i++) {
            String username = names[i];
            if (userRepository.existsByUsername(username)) {
                continue;
            }
            User user = new User(
                    username,
                    username + "@example.com",
                    passwordEncoder.encode("Password@123"),
                    gameProperties.getDefaultTurns() + (i % 3),
                    Role.USER
            );
            user.setScore((names.length - i) * 2);
            userRepository.save(user);
        }
    }
}
