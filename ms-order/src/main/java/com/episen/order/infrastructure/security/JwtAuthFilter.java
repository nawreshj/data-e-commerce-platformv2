package com.episen.order.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.resetBuffer();
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
        response.flushBuffer();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Actuator non sécurisé
        if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("JWT filter triggered for path={}", path);

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header path={}", path);
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = auth.substring(7);

        try {
            Claims claims = jwtTokenService.validate(token);

            Object userId = claims.get("userId");
            List<String> roles = claims.get("roles", List.class);

            log.debug("JWT valid userId={} roles={} path={}", userId, roles, path);

            // Attributs request (utile pour services métier)
            request.setAttribute("userId", userId);
            request.setAttribute("roles", roles);

            var authorities = (roles == null)
                    ? List.<SimpleGrantedAuthority>of()
                    : roles.stream()
                           .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                           .map(SimpleGrantedAuthority::new)
                           .collect(Collectors.toList());

            var authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (JwtTokenService.TokenExpiredException ex) {
            log.warn("JWT expired path={}", path);
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Token expired");
            return;

        } catch (JwtTokenService.TokenInvalidException ex) {
            log.warn("JWT invalid path={}", path);
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}