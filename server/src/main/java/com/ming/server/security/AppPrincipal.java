package com.ming.server.security;

/**
 * Principal stored in the SecurityContext after JWT authentication.
 *
 * @param userId   当前生效的用户ID（管理员 token 映射到站长用户的 ID）
 * @param username 用户名
 * @param admin    是否为站长后台管理员（独立 admin_users 表）
 */
public record AppPrincipal(Long userId, String username, boolean admin) {

    public AppPrincipal(Long userId, String username) {
        this(userId, username, false);
    }

    public boolean isAdmin() {
        return admin;
    }
}