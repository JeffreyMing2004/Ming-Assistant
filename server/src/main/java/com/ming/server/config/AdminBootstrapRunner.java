package com.ming.server.config;

import com.ming.server.admin.AdminUser;
import com.ming.server.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 站长后台账号引导注册：
 * 启动时若配置了 APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD 环境变量，
 * 则注册（不存在时创建）或重置（已存在时更新密码）对应管理员账号。
 * 管理员账号独立存放于 admin_users 表，与 App 用户表分离。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap-username:}")
    private String bootstrapUsername;

    @Value("${app.admin.bootstrap-password:}")
    private String bootstrapPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (bootstrapUsername == null || bootstrapUsername.isBlank()
                || bootstrapPassword == null || bootstrapPassword.isBlank()) {
            log.info("未配置 APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD，跳过站长账号引导");
            return;
        }
        String username = bootstrapUsername.trim();
        AdminUser admin = adminUserRepository.findByUsername(username).orElse(null);
        boolean created = admin == null;
        if (created) {
            admin = new AdminUser();
            admin.setUsername(username);
            admin.setDisplayName(username);
        }
        admin.setPasswordHash(passwordEncoder.encode(bootstrapPassword));
        adminUserRepository.save(admin);
        log.info("站长后台账号[{}]{}（admin_users 表，与用户表分离）",
                username, created ? "注册成功" : "已存在，密码已重置");
    }
}