package com.ipl.analysis.dto;

public record AuthDto(
    String username,
    String password,
    String token,
    String role
) {
    public record LoginRequest(String username, String password) {}
    public record RegisterRequest(String username, String password, String role) {}
    public record AuthResponse(String token, String username, String role) {}
}
