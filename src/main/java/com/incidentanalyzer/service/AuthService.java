package com.incidentanalyzer.service;

import com.incidentanalyzer.dto.auth.LoginRequest;
import com.incidentanalyzer.dto.auth.RefreshTokenRequest;
import com.incidentanalyzer.dto.auth.RegisterRequest;
import com.incidentanalyzer.dto.auth.TokenResponse;
import com.incidentanalyzer.model.User;
import com.incidentanalyzer.security.JwtService;
import com.incidentanalyzer.security.JwtTokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public TokenResponse register(RegisterRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .password(request.password())
            .role(com.incidentanalyzer.model.UserRole.ENGINEER)
                .build();
        User saved = userService.createUser(user);
        return issueTokens(saved);
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        User user = userService.findByEmail(request.email().toLowerCase());
        return issueTokens(user);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        if (!jwtService.isTokenValid(request.refreshToken(), JwtTokenType.REFRESH)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String email = jwtService.extractEmail(request.refreshToken());
        User user = userService.findByEmail(email);
        return issueTokens(user);
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.accessTokenTtlSeconds(),
                jwtService.refreshTokenTtlSeconds(),
                userService.toResponse(user));
    }
}
