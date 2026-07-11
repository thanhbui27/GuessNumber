package com.guessgame.mapper;

import com.guessgame.dto.user.CurrentUserResponse;
import com.guessgame.dto.user.UserResponse;
import com.guessgame.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getScore(), user.getTurns(), user.getRole());
    }

    public CurrentUserResponse toCurrentResponse(User user, long rank) {
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getScore(), user.getTurns(), rank, user.getRole(), user.getCreatedAt());
    }
}
