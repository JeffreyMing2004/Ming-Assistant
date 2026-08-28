package com.ming.server.auth;

import com.ming.server.auth.dto.AuthResponse;
import com.ming.server.auth.dto.LoginRequest;
import com.ming.server.auth.dto.RegisterRequest;
import com.ming.server.config.ApiException;
import com.ming.server.security.JwtService;
import com.ming.server.user.User;
import com.ming.server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw ApiException.badRequest("两次输入的密码不一致");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw ApiException.badRequest("用户名已被注册");
        }

        User user = new User();
        user.setBilibiliUid(req.getBilibiliUid());
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);

        return AuthResponse.from(user, jwtService.generate(user));
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("用户名或密码错误"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized("用户名或密码错误");
        }
        return AuthResponse.from(user, jwtService.generate(user));
    }

    public AuthResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("用户不存在"));
        return new AuthResponse(user.getId(), user.getUsername(), user.getBilibiliUid(), null);
    }
}