package com.ming.server.live;

import com.fasterxml.jackson.databind.JsonNode;
import com.ming.server.config.ApiException;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 代理 B站公开直播间状态接口，带 60 秒缓存以防限流。
 * 接口：GET https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid={uid}
 */
@Service
public class BilibiliLiveService {

    private static final long CACHE_TTL_MS = 60_000;

    @Value("${app.bilibili.uid}")
    private String bilibiliUid;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.live.bilibili.com")
            .defaultHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36")
            .build();

    private volatile LiveStatus cached;
    private volatile long cachedAtMillis;

    public LiveStatus getLiveStatus() {
        long now = System.currentTimeMillis();
        if (cached != null && now - cachedAtMillis < CACHE_TTL_MS) {
            return cached;
        }
        LiveStatus fresh = fetchFromBilibili();
        cached = fresh;
        cachedAtMillis = System.currentTimeMillis();
        return fresh;
    }

    private LiveStatus fetchFromBilibili() {
        try {
            JsonNode root = restClient.get()
                    .uri("/room/v1/Room/getRoomInfoOld?mid={mid}", bilibiliUid)
                    .retrieve()
                    .body(JsonNode.class);
            if (root == null || root.path("code").asInt() != 0) {
                throw ApiException.badRequest("B站接口返回异常");
            }
            JsonNode data = root.path("data");
            return new LiveStatus(
                    data.path("roomid").asLong(),
                    data.path("liveStatus").asInt(),
                    data.path("title").asText(),
                    data.path("cover").asText(""),
                    data.path("online").asLong(),
                    data.path("url").asText(""),
                    Instant.now());
        } catch (RuntimeException e) {
            if (e instanceof ApiException) {
                throw e;
            }
            throw new ApiException(HttpStatus.BAD_GATEWAY, "获取B站直播状态失败");
        }
    }
}