package com.episen.order.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = auth.substring(7);

        try {
            jwtTokenService.validate(token); // signature + exp
            // (optionnel) ici tu peux mettre Authentication dans le context si tu veux
            filterChain.doFilter(request, response);

        } catch (JwtTokenService.TokenExpiredException ex) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token expired"); // demandé par le prof
        } catch (JwtTokenService.TokenInvalidException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }
}