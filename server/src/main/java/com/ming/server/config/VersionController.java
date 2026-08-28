package com.ming.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** App 在线版本检测：返回当前最新发布版本号与下载地址（发布新版本时修改配置即可）。 */
@RestController
@RequestMapping("/api/app/version")
public class VersionController {

    @Value("${app.release.latest-version:}")
    private String latestVersion;

    @Value("${app.release.latest-code:0}")
    private int latestCode;

    @Value("${app.release.apk-url:}")
    private String apkUrl;

    @Value("${app.release.update-note:}")
    private String updateNote;

    @GetMapping
    public AppVersionResponse version() {
        return new AppVersionResponse(
                latestVersion == null ? "" : latestVersion,
                latestCode,
                apkUrl == null ? "" : apkUrl,
                updateNote == null ? "" : updateNote);
    }

    /** latestVersion：版本号（如 1.2）；latestCode：versionCode，用于 App 侧数值比较。 */
    public record AppVersionResponse(String latestVersion, int latestCode, String apkUrl, String updateNote) {
    }
}