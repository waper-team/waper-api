package com.waper.waperapi.controller;

import com.waper.waperapi.dto.AuthRequest;
import com.waper.waperapi.dto.AuthResponse;
import com.waper.waperapi.dto.ApiError;
import com.waper.waperapi.dto.UserResponse;
import com.waper.waperapi.model.User;
import com.waper.waperapi.repository.UserRepository;
import com.waper.waperapi.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(@RequestBody AuthRequest request) {
        if (request.email() == null || request.password() == null) {
            return ResponseEntity.badRequest().body(new ApiError("Email y password son obligatorios"));
        }

        User user = userRepository.findByEmailIgnoreCase(request.email().trim()).orElse(null);
        if (user == null || !passwordMatches(request.password(), user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("Credenciales invalidas"));
        }

        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("STUDENT");
            user = userRepository.save(user);
        }

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(
            new AuthResponse(token, jwtService.getExpirationSeconds(), UserResponse.from(user))
        );
    }

    private boolean passwordMatches(String rawPassword, User user) {
        String storedPassword = user.getPassword();
        if (storedPassword == null) return false;

        if (storedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (!storedPassword.equals(rawPassword)) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        return true;
    }
}
