package com.waper.waperapi.controller;

import com.waper.waperapi.model.User;
import com.waper.waperapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    @Test
    void testGetUserByIdFound() {
        User user = new User();
        user.setId("123");
        user.setUsername("testuser");

        when(userRepository.findById("123")).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUserById("123");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        verify(userRepository, times(1)).findById("123");
    }

    @Test
    void testCreateUserSuccess() {
        User user = new User();
        user.setId("123");
        user.setUsername("newuser");

        when(userRepository.existsById("123")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<User> response = userController.createUser(user);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("newuser", response.getBody().getUsername());
        verify(userRepository, times(1)).existsById("123");
        verify(userRepository, times(1)).save(user);
    }
}
