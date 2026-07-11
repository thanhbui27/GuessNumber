package com.guessgame.service;

import com.guessgame.config.GameProperties;
import com.guessgame.dto.auth.AuthResponse;
import com.guessgame.dto.auth.LoginRequest;
import com.guessgame.dto.auth.RegisterRequest;
import com.guessgame.entity.User;
import com.guessgame.enums.Role;
import com.guessgame.exception.ApiException;
import com.guessgame.mapper.UserMapper;
import com.guessgame.repository.UserRepository;
import com.guessgame.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final GameProperties gameProperties;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserMapper userMapper,
                       GameProperties gameProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.gameProperties = gameProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username da ton tai.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email da ton tai.");
        }
        User user = new User(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                gameProperties.getDefaultTurns(),
                Role.USER
        );
        User saved = userRepository.save(user);
        return authResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password()));
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByUsernameOrEmail(request.usernameOrEmail())
                        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Ten dang nhap hoac mat khau khong chinh xac.")));
        return authResponse(user);
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), "Bearer", jwtService.expiresInSeconds(), userMapper.toResponse(user));
    }
}
