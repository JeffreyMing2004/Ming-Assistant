package com.ming.server.song.qq.dto;

import java.util.List;

/** 导入前预览：歌单信息 + 全部歌曲 + 与现有歌单的重复数。 */
public record QqPreview(String title, int total, int duplicate, List<QqTrack> tracks) {
}