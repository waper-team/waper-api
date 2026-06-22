package com.waper.waperapi.dto;

import java.util.List;

public record UserUpdateRequest(
    String username,
    String name,
    String email,
    String password,
    String bio,
    String profileImage,
    List<String> interests
) {
}
