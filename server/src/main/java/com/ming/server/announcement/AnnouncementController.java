package com.ming.server.announcement;

import com.ming.server.announcement.dto.AnnouncementRequest;
import com.ming.server.announcement.dto.AnnouncementResponse;
import com.ming.server.config.ApiException;
import com.ming.server.security.AppPrincipal;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 首页公告：App 公开读取，站长后台管理员编辑。 */
@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementRepository repository;

    @GetMapping
    public AnnouncementResponse get() {
        return repository.findById(1L)
                .map(this::toResponse)
                .orElseGet(AnnouncementResponse::empty);
    }

    @PutMapping
    public AnnouncementResponse update(@AuthenticationPrincipal AppPrincipal principal,
                                       @Valid @RequestBody AnnouncementRequest request) {
        if (principal == null || !principal.admin()) {
            throw ApiException.forbidden("仅站长后台管理员可编辑公告");
        }
        String text = request.text() == null ? "" : request.text();
        Announcement row = repository.findById(1L).orElseGet(() -> {
            Announcement a = new Announcement();
            a.setId(1L);
            return a;
        });
        row.setText(text);
        row.setUpdatedAt(Instant.now());
        return toResponse(repository.save(row));
    }

    private AnnouncementResponse toResponse(Announcement a) {
        return new AnnouncementResponse(
                a.getText() == null ? "" : a.getText(),
                a.getUpdatedAt() == null ? null : a.getUpdatedAt().toString());
    }
}