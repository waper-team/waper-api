package com.waper.waperapi.controller;

import com.waper.waperapi.dto.AuthRequest;
import com.waper.waperapi.dto.AuthResponse;
import com.waper.waperapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testTokenSuccess() {
        AuthRequest request = new AuthRequest("admin", "password123");
        when(jwtService.generateToken("admin")).thenReturn("mock-jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        ResponseEntity<?> response = authController.token(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthResponse);
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("mock-jwt-token", authResponse.token());
        assertEquals(3600L, authResponse.expiresInSeconds());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken("admin");
        verify(jwtService, times(1)).getExpirationSeconds();
    }

    @Test
    void testTokenUnauthorized() {
        AuthRequest request = new AuthRequest("admin", "wrongpassword");
        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authenticationManager)
            .authenticate(any(UsernamePasswordAuthenticationToken.class));

        ResponseEntity<?> response = authController.token(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Credenciales invalidas", response.getBody());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService);
    }
}
