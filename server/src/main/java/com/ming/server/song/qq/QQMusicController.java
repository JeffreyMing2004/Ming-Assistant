package com.ming.server.song.qq;

import com.ming.server.security.AppPrincipal;
import com.ming.server.song.qq.dto.QqImportResult;
import com.ming.server.song.qq.dto.QqPlaylistRequest;
import com.ming.server.song.qq.dto.QqPreview;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs/qq")
@RequiredArgsConstructor
public class QQMusicController {

    private final QQMusicService qqMusicService;

    @PostMapping("/preview")
    public QqPreview preview(@AuthenticationPrincipal AppPrincipal principal,
                             @Valid @RequestBody QqPlaylistRequest request) {
        return qqMusicService.preview(principal.userId(), request.getUrl());
    }

    @PostMapping("/import")
    public QqImportResult importPlaylist(@AuthenticationPrincipal AppPrincipal principal,
                                         @Valid @RequestBody QqPlaylistRequest request) {
        return qqMusicService.importPlaylist(principal.userId(), request.getUrl());
    }
}