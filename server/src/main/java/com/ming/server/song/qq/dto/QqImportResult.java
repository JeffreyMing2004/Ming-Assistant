package com.ming.server.song.qq.dto;

/** 导入结果。 */
public record QqImportResult(int imported, int skipped, String title) {
}