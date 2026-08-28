package com.ming.server.gift.dto;

import com.ming.server.gift.GiftRecord;
import java.time.Instant;

/**
 * 管理端舰礼视图：包含提交人用户名与后台登记的快递单号。
 */
public record AdminGiftView(
        Long id,
        Long userId,
        String username,
        String nickname,
        String bilibiliUid,
        String phone,
        String address,
        String giftType,
        String trackingNumber,
        Instant createdAt) {

    public static AdminGiftView from(GiftRecord g, String username) {
        return new AdminGiftView(
                g.getId(),
                g.getUserId(),
                username,
                g.getNickname(),
                g.getBilibiliUid(),
                g.getPhone(),
                g.getAddress(),
                g.getGiftType(),
                g.getTrackingNumber(),
                g.getCreatedAt());
    }
}