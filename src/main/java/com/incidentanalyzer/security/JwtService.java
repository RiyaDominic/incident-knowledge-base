package com.incidentanalyzer.security;

import com.incidentanalyzer.config.JwtProperties;
import com.incidentanalyzer.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        return buildToken(user, jwtProperties.accessTokenTtl(), JwtTokenType.ACCESS);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, jwtProperties.refreshTokenTtl(), JwtTokenType.REFRESH);
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public JwtTokenType extractTokenType(String token) {
        return JwtTokenType.valueOf(parseClaims(token).get("tokenType", String.class));
    }

    public boolean isTokenValid(String token, JwtTokenType expectedType) {
        try {
            return extractTokenType(token) == expectedType && parseClaims(token).getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public long accessTokenTtlSeconds() {
        return jwtProperties.accessTokenTtl().toSeconds();
    }

    public long refreshTokenTtlSeconds() {
        return jwtProperties.refreshTokenTtl().toSeconds();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey()).build().parseSignedClaims(token).getPayload();
    }

    private String buildToken(User user, java.time.Duration ttl, JwtTokenType tokenType) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("tokenType", tokenType.name())
                .signWith(secretKey())
                .compact();
    }

    private SecretKey secretKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to initialize JWT signing key", ex);
        }
    }
}
