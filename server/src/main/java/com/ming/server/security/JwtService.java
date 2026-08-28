package com.ming.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * JWT 签发与解析。
 * token 类型（claim "typ"）：
 *  - "usr"：App 用户 token，uid = users.id；
 *  - "adm"：站长后台管理员 token，uid = 站长对应的 users.id（管理员以此身份代行站长操作）。
 * subject 一律为用户名。
 */
@Service
public class JwtService {

    public static final String TYPE_USER = "usr";
    public static final String TYPE_ADMIN = "adm";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-hours:168}")
    private long expirationHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private String build(String subject, Long uid, String type) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .claim("typ", type)
                .claim("uid", uid)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationHours * 3600_000))
                .signWith(key())
                .compact();
    }

    /** App 用户登录 token。 */
    public String generate(Long userId, String username) {
        return build(username, userId, TYPE_USER);
    }

    /** 站长后台管理员 token：以站长用户（owner）的身份行使操作。 */
    public String generateAdmin(String adminUsername, Long ownerUserId) {
        return build(adminUsername, ownerUserId, TYPE_ADMIN);
    }

    /** 解析 JWT 并返回关键 claim；非法 token 抛异常。 */
    public TokenClaims parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String type = claims.get("typ", String.class);
        if (type == null || type.isBlank()) {
            // 兼容旧 token
            type = TYPE_USER;
        }
        Number uid = claims.get("uid", Number.class);
        if (uid == null) {
            throw new IllegalArgumentException("token 缺少 uid");
        }
        return new TokenClaims(type, uid.longValue(), claims.getSubject());
    }

    /** 已解析的 token 载荷。 */
    public record TokenClaims(String type, Long uid, String subject) {
    }
}