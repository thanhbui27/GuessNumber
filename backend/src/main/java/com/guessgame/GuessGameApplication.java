package com.guessgame;

import com.guessgame.config.GameProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GameProperties.class)
public class GuessGameApplication {
    public static void main(String[] args) {
        SpringApplication.run(GuessGameApplication.class, args);
    }
}
