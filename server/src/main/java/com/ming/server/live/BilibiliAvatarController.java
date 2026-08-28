package com.ming.server.live;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代理 B站公开的用户信息接口（头像等），供 App 内填写 B站UID 后获取头像。
 */
@RestController
@RequestMapping("/api/bilibili")
@RequiredArgsConstructor
public class BilibiliAvatarController {

    private final BilibiliLiveService bilibiliLiveService;

    @GetMapping("/avatar")
    public BilibiliAvatarResponse avatar(@RequestParam(value = "uid", defaultValue = "") String uid) {
        return new BilibiliAvatarResponse(bilibiliLiveService.getAvatarFace(uid.trim()));
    }
}