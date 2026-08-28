package com.ming.server.security;

/**
 * Principal stored in the SecurityContext after JWT authentication.
 */
public record AppPrincipal(Long userId, String username) {
}