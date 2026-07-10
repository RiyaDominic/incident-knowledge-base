package com.incidentanalyzer.service;

import com.incidentanalyzer.dto.auth.LoginRequest;
import com.incidentanalyzer.dto.auth.RegisterRequest;
import com.incidentanalyzer.dto.auth.TokenResponse;
import com.incidentanalyzer.dto.auth.UserResponse;
import com.incidentanalyzer.model.User;
import com.incidentanalyzer.model.UserRole;
import com.incidentanalyzer.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerIssuesTokenPair() {
        RegisterRequest request = new RegisterRequest("Ada", "ada@example.com", "strong-password", UserRole.ENGINEER);
        User saved = User.builder().id("user-1").name("Ada").email("ada@example.com").role(UserRole.ENGINEER).build();
        when(userService.createUser(any())).thenReturn(saved);
        when(jwtService.generateAccessToken(saved)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(saved)).thenReturn("refresh-token");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);
        when(jwtService.refreshTokenTtlSeconds()).thenReturn(604800L);
        when(userService.toResponse(saved)).thenReturn(new UserResponse("user-1", "Ada", "ada@example.com", UserRole.ENGINEER, null));

        TokenResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().email()).isEqualTo("ada@example.com");
        verify(userService).createUser(any());
    }

    @Test
    void loginAuthenticatesAndReturnsTokens() {
        LoginRequest request = new LoginRequest("ada@example.com", "strong-password");
        User user = User.builder().id("user-1").name("Ada").email("ada@example.com").role(UserRole.ENGINEER).build();
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);
        when(jwtService.refreshTokenTtlSeconds()).thenReturn(604800L);
        when(userService.findByEmail("ada@example.com")).thenReturn(user);
        when(userService.toResponse(user)).thenReturn(new UserResponse("user-1", "Ada", "ada@example.com", UserRole.ENGINEER, null));

        authService.login(request);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
