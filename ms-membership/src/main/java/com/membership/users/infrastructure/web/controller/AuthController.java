package com.membership.users.infrastructure.web.controller;

import com.membership.users.application.dto.LoginRequestDto;
import com.membership.users.application.dto.LoginResponseDto;
import com.membership.users.domain.entity.User;
import com.membership.users.domain.repository.UserRepository;
import com.membership.users.infrastructure.security.JwtIssuerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtIssuerService jwtIssuerService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtIssuerService jwtIssuerService) {
        this.userRepository = userRepository;
        this.jwtIssuerService = jwtIssuerService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto req) {

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        
        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // roles minimal (si tu n'as pas de champ roles en DB, mets juste USER)
        List<String> roles = List.of("USER");

        //long expiresIn = 3600;
        long expiresIn = 60;
        String token = jwtIssuerService.generateToken(user.getId(), user.getEmail(), roles, expiresIn);

        return ResponseEntity.ok(new LoginResponseDto(token, expiresIn));
    }
}