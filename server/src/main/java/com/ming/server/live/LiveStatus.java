package com.ming.server.live;

import java.time.Instant;

/**
 * B站直播间公开状态（来自 getRoomInfoOld 接口的裁剪结果）。
 */
public record LiveStatus(
        long roomId,
        int liveStatus,
        String title,
        String cover,
        long online,
        String url,
        Instant checkedAt) {

    public boolean isLive() {
        return liveStatus == 1;
    }
}