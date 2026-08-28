package com.ming.server.live;

/**
 * B站用户头像（数据来自 B站 live_user Master/info 公开接口的裁剪结果）。
 *
 * @param face 头像URL，无法获取时为空串
 */
public record BilibiliAvatarResponse(String face) {
}