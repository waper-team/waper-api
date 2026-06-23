package com.waper.waperapi.controller;

import com.waper.waperapi.dto.ApiError;
import com.waper.waperapi.dto.UserCreateRequest;
import com.waper.waperapi.dto.UserResponse;
import com.waper.waperapi.dto.UserUpdateRequest;
import com.waper.waperapi.model.User;
import com.waper.waperapi.repository.UserRepository;
import com.waper.waperapi.validation.UserInputValidator;
import java.net.URI;
import java.util.ArrayList;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
        @PathVariable String id,
        Authentication authentication
    ) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (!user.getEmail().equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(403).body(new ApiError("No puedes acceder a otro perfil"));
        }
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {
        String validationError = UserInputValidator.validateRegistration(
            request.name(),
            request.username(),
            request.email(),
            request.password()
        );
        if (validationError != null) {
            return ResponseEntity.badRequest().body(new ApiError(validationError));
        }

        String email = request.email().trim().toLowerCase();
        String username = request.username().trim();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(409).body(new ApiError("El email ya esta registrado"));
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(409).body(new ApiError("El username ya esta registrado"));
        }

        User user = new User(
            null,
            username,
            request.name().trim(),
            email,
            passwordEncoder.encode(request.password()),
            "",
            "",
            new ArrayList<>(),
            0,
            0,
            "STUDENT"
        );

        User savedUser = userRepository.save(user);
        return ResponseEntity
            .created(URI.create("/api/users/" + savedUser.getId()))
            .body(UserResponse.from(savedUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
        @PathVariable String id,
        @RequestBody UserUpdateRequest request,
        Authentication authentication
    ) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingUser.getEmail().equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(403).body(new ApiError("No puedes editar otro perfil"));
        }

        String validationError = UserInputValidator.validateUpdate(
            request.name(),
            request.username(),
            request.email(),
            request.password(),
            request.bio()
        );
        if (validationError != null) {
            return ResponseEntity.badRequest().body(new ApiError(validationError));
        }

        if (!isBlank(request.email())) {
            String email = request.email().trim().toLowerCase();
            boolean belongsToAnotherUser = userRepository.findByEmailIgnoreCase(email)
                .filter(user -> !user.getId().equals(id))
                .isPresent();
            if (belongsToAnotherUser) {
                return ResponseEntity.status(409).body(new ApiError("El email ya esta registrado"));
            }
            existingUser.setEmail(email);
        }

        if (!isBlank(request.username())) {
            String username = request.username().trim();
            boolean belongsToAnotherUser = userRepository.findByUsername(username)
                .filter(user -> !user.getId().equals(id))
                .isPresent();
            if (belongsToAnotherUser) {
                return ResponseEntity.status(409).body(new ApiError("El username ya esta registrado"));
            }
            existingUser.setUsername(username);
        }

        if (!isBlank(request.name())) existingUser.setName(request.name().trim());
        if (!isBlank(request.password())) {
            existingUser.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.bio() != null) existingUser.setBio(request.bio());
        if (request.profileImage() != null) existingUser.setProfileImage(request.profileImage());
        if (request.interests() != null) existingUser.setInterests(request.interests());

        return ResponseEntity.ok(UserResponse.from(userRepository.save(existingUser)));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
