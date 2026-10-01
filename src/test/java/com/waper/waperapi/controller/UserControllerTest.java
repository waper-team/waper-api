package com.waper.waperapi.controller;

import com.waper.waperapi.dto.UserCreateRequest;
import com.waper.waperapi.dto.UserResponse;
import com.waper.waperapi.model.User;
import com.waper.waperapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    @Test
    void testGetUserByIdFound() {
        User user = new User();
        user.setId("user-123");
        user.setUsername("john_doe");
        user.setEmail("test@test.com");

        when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("test@test.com");

        ResponseEntity<?> response = userController.getUserById("user-123", authentication);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        UserResponse body = (UserResponse) response.getBody();
        assertEquals("user-123", body.id());
        assertEquals("john_doe", body.username());

        verify(userRepository, times(1)).findById("user-123");
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserById("999", authentication);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());

        verify(userRepository, times(1)).findById("999");
    }

    @Test
    void testCreateUserSuccess() {
        UserCreateRequest request = new UserCreateRequest("newuser", "password123", "email@test.com", "User Full Name");

        User savedUser = new User();
        savedUser.setId("generated-id");
        savedUser.setUsername("newuser");
        savedUser.setEmail("email@test.com");

        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        ResponseEntity<?> response = userController.createUser(request);

        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());

        UserResponse body = (UserResponse) response.getBody();
        assertEquals("generated-id", body.id());
        assertEquals("newuser", body.username());

        verify(userRepository, times(1)).save(any(User.class));
    }
}
