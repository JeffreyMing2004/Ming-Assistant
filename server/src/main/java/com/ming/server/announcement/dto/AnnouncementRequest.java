package com.ming.server.announcement.dto;

import jakarta.validation.constraints.Size;

/** 管理员编辑公告。text 为空串表示清空公告。 */
public record AnnouncementRequest(@Size(max = 2000) String text) {
}