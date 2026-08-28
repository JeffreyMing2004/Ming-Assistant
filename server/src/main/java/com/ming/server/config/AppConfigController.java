package com.ming.server.config;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 前端（管理端等）读取的基础配置。 */
@RestController
@RequestMapping("/api/app/config")
public class AppConfigController {

    @Value("${app.qq.owner-username:testuser}")
    private String ownerUsername;

    @GetMapping
    public Map<String, String> config() {
        return Map.of("ownerUsername", ownerUsername);
    }
}