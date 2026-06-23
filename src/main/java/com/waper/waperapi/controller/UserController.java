package com.waper.waperapi.controller;

import com.waper.waperapi.model.User;
import com.waper.waperapi.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (user.getId() != null && userRepository.existsById(user.getId())) {
            return ResponseEntity.badRequest().build();
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User userUpdate) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (userUpdate.getUsername() != null) existingUser.setUsername(userUpdate.getUsername());
                    if (userUpdate.getName() != null) existingUser.setName(userUpdate.getName());
                    if (userUpdate.getEmail() != null) existingUser.setEmail(userUpdate.getEmail());
                    if (userUpdate.getPassword() != null) existingUser.setPassword(userUpdate.getPassword());
                    if (userUpdate.getBio() != null) existingUser.setBio(userUpdate.getBio());
                    if (userUpdate.getProfileImage() != null) existingUser.setProfileImage(userUpdate.getProfileImage());
                    if (userUpdate.getInterests() != null) existingUser.setInterests(userUpdate.getInterests());
                    existingUser.setFriendsCount(userUpdate.getFriendsCount());
                    existingUser.setStreakCount(userUpdate.getStreakCount());
                    if (userUpdate.getRole() != null) existingUser.setRole(userUpdate.getRole());
                    User updatedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
