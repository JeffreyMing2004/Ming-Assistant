package com.ming.server.song.qq;

import com.ming.server.song.qq.dto.QqImportResult;
import com.ming.server.user.User;
import com.ming.server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * QQ音乐歌单后台自动同步：
 * 定期抓取配置的歌单并与数据库对比，数据库中缺失的歌曲自动加入（按 歌名+歌手 去重）。
 * 无需手动「从QQ音乐导入」。
 */
@Component
@RequiredArgsConstructor
public class QqAutoSync {

    private static final Logger log = LoggerFactory.getLogger(QqAutoSync.class);

    private final QQMusicService qqMusicService;
    private final UserRepository userRepository;

    @Value("${app.qq.sync-enabled:true}")
    private boolean syncEnabled;

    @Value("${app.qq.playlist:}")
    private String playlist;

    @Value("${app.qq.owner-username:testuser}")
    private String ownerUsername;

    @Scheduled(fixedDelayString = "${app.qq.sync-interval-ms:1800000}",
            initialDelayString = "${app.qq.sync-interval-ms:1800000}")
    public void sync() {
        if (!syncEnabled || playlist == null || playlist.isBlank()) {
            return;
        }
        User owner = userRepository.findByUsername(ownerUsername).orElse(null);
        if (owner == null) {
            log.warn("QQ歌单自动同步跳过：找不到账号 {}（可在 application.yml 的 app.qq.owner-username 配置）", ownerUsername);
            return;
        }
        try {
            QqImportResult result = qqMusicService.importPlaylist(owner.getId(), playlist);
            if (result.imported() > 0) {
                log.info("QQ歌单自动同步：歌单《{}》新增 {} 首，跳过重复 {} 首",
                        result.title(), result.imported(), result.skipped());
            } else if (result.skipped() > 0) {
                log.debug("QQ歌单自动同步：歌单《{}》无新增，{} 首均已在歌单中", result.title(), result.skipped());
            } else {
                log.warn("QQ歌单自动同步：歌单《{}》没有读取到歌曲", result.title());
            }
        } catch (Exception e) {
            log.warn("QQ歌单自动同步失败：{}", e.getMessage());
        }
    }
}