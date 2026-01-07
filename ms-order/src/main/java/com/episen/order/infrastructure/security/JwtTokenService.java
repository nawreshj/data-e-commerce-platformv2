package com.episen.order.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import org.springframework.stereotype.Service;

import java.security.PublicKey;

@Service
public class JwtTokenService {

    private final PublicKey publicKey;

    public JwtTokenService(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public Claims validate(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException();
        } catch (Exception ex) {
            throw new TokenInvalidException();
        }
    }

    public static class TokenExpiredException extends RuntimeException {}
    public static class TokenInvalidException extends RuntimeException {}
}