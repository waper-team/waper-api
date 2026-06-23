package com.waper.waperapi.dto;

import com.waper.waperapi.model.User;
import java.util.List;

public record UserResponse(
    String id,
    String username,
    String name,
    String email,
    String bio,
    String profileImage,
    List<String> interests,
    int friendsCount,
    int streakCount,
    String role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getName(),
            user.getEmail(),
            user.getBio(),
            user.getProfileImage(),
            user.getInterests(),
            user.getFriendsCount(),
            user.getStreakCount(),
            user.getRole()
        );
    }
}
