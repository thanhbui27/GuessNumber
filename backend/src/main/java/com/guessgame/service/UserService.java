package com.guessgame.service;

import com.guessgame.dto.user.CurrentUserResponse;
import com.guessgame.entity.User;
import com.guessgame.exception.ApiException;
import com.guessgame.mapper.UserMapper;
import com.guessgame.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(Long userId) {
        User user = findUser(userId);
        long rank = userRepository.countUsersAhead(user.getScore(), user.getCreatedAt(), user.getId()) + 1;
        return userMapper.toCurrentResponse(user, rank);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Khong tim thay user."));
    }
}
