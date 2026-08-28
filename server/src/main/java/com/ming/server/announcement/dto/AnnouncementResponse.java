package com.ming.server.announcement.dto;

/** App 首页公告内容。text 为空串表示暂无公告。 */
public record AnnouncementResponse(String text, String updatedAt) {

    public static AnnouncementResponse empty() {
        return new AnnouncementResponse("", null);
    }
}