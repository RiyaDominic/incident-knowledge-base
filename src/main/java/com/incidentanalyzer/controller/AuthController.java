package com.incidentanalyzer.controller;

import com.incidentanalyzer.dto.auth.LoginRequest;
import com.incidentanalyzer.dto.auth.RefreshTokenRequest;
import com.incidentanalyzer.dto.auth.RegisterRequest;
import com.incidentanalyzer.dto.auth.TokenResponse;
import com.incidentanalyzer.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a user account and returns access and refresh tokens.")
    @ApiResponse(responseCode = "200", description = "User registered")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in a user", description = "Authenticates credentials and returns a JWT token pair.")
    @ApiResponse(responseCode = "200", description = "Logged in")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh a token pair", description = "Validates a refresh token and issues a new token pair.")
    @ApiResponse(responseCode = "200", description = "Token refreshed")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}
