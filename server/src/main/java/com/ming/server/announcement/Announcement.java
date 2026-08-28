package com.ming.server.announcement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 首页公告（单行表，固定 id=1）：用于通知「今日是否开播」等信息。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    private Long id = 1L;

    /** 公告文本，空串表示暂无公告。 */
    @Column(name = "text", columnDefinition = "TEXT")
    private String text = "";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}