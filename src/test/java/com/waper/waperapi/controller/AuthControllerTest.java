package com.waper.waperapi.controller;

import com.waper.waperapi.dto.AuthRequest;
import com.waper.waperapi.model.User;
import com.waper.waperapi.repository.UserRepository;
import com.waper.waperapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId("user-123");
        mockUser.setUsername("john_doe");
        mockUser.setEmail("john@test.com");
        mockUser.setPassword("$2a$10$e8Obx/e4H2X8/exampleBCryptHash");
        mockUser.setRole("STUDENT");
    }

    @Test
    void testAuthenticateSuccess() {
        AuthRequest request = new AuthRequest("john@test.com", "password123");

        when(userRepository.findByEmailIgnoreCase("john@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", mockUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        ResponseEntity<?> response = authController.token(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(userRepository, times(1)).findByEmailIgnoreCase("john@test.com");
        verify(jwtService, times(1)).generateToken(mockUser);
    }
}
