package com.waper.waperapi.dto;

public record UserCreateRequest(
    String username,
    String name,
    String email,
    String password
) {
}
