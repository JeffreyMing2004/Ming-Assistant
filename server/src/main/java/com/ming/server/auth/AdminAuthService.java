package com.ming.server.auth;

import com.ming.server.admin.AdminUser;
import com.ming.server.admin.AdminUserRepository;
import com.ming.server.auth.dto.AdminLoginRequest;
import com.ming.server.auth.dto.AdminLoginResponse;
import com.ming.server.config.ApiException;
import com.ming.server.security.JwtService;
import com.ming.server.user.User;
import com.ming.server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.qq.owner-username:testuser}")
    private String ownerUsername;

    /**
     * 站长后台登录（管理员账号存于独立的 admin_users 表）。
     * 登录成功后签发管理员 JWT，以站长用户（owner）身份行使后台操作。
     */
    public AdminLoginResponse login(AdminLoginRequest req) {
        AdminUser admin = adminUserRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("管理员账号或密码错误"));
        if (!passwordEncoder.matches(req.getPassword(), admin.getPasswordHash())) {
            throw ApiException.unauthorized("管理员账号或密码错误");
        }

        Long ownerUserId = userRepository.findByUsername(ownerUsername)
                .map(User::getId)
                .orElseThrow(() -> ApiException.badRequest(
                        "未配置站长用户（app.qq.owner-username，当前为 " + ownerUsername + "）"));

        String token = jwtService.generateAdmin(admin.getUsername(), ownerUserId);
        return new AdminLoginResponse(
                token,
                admin.getUsername(),
                admin.getDisplayName() == null || admin.getDisplayName().isBlank()
                        ? admin.getUsername() : admin.getDisplayName(),
                ownerUserId,
                ownerUsername);
    }
}