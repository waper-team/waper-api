package com.waper.waperapi.dto;

public record AuthResponse(String token, long expiresInSeconds) {
}
