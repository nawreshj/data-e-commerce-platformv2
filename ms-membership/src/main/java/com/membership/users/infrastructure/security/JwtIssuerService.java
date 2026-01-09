package com.membership.users.infrastructure.security;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtIssuerService {

    private final PrivateKey privateKey;

    public JwtIssuerService(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String generateToken(Long userId, String email, List<String> roles, long expiresInSeconds) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiresInSeconds);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }
}