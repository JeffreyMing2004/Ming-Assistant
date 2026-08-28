package com.ming.server.auth.dto;

import com.ming.server.user.User;

public record AuthResponse(Long userId, String username, String bilibiliUid, String token) {

    public static AuthResponse from(User user, String token) {
        return new AuthResponse(user.getId(), user.getUsername(), user.getBilibiliUid(), token);
    }
}