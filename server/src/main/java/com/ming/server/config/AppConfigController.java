package com.ming.server.config;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 前端（管理端等）读取的基础配置。公开接口，仅返回非敏感信息。 */
@RestController
@RequestMapping("/api/app/config")
public class AppConfigController {

    @Value("${app.qq.owner-username:testuser}")
    private String ownerUsername;

    @Value("${app.geetest.captcha-id:}")
    private String captchaId;

    @GetMapping
    public Map<String, String> config() {
        return Map.of(
                "ownerUsername", ownerUsername,
                "captchaId", captchaId == null ? "" : captchaId);
    }
}