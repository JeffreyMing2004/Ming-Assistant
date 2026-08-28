package com.ming.server.auth.dto;

/**
 * 站长后台登录响应。
 *
 * @param token        管理员 JWT（代行站长用户身份）
 * @param username     管理员账号名
 * @param name         管理员显示名
 * @param userId       站长用户 ID（管理员 token 映射到的用户）
 * @param ownerUsername 站长（直播歌单归属）用户名
 */
public record AdminLoginResponse(
        String token,
        String username,
        String name,
        Long userId,
        String ownerUsername) {
}