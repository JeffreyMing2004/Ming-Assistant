package com.ming.server.live;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor
public class LiveController {

    private final BilibiliLiveService bilibiliLiveService;

    @GetMapping("/status")
    public LiveStatus status() {
        return bilibiliLiveService.getLiveStatus();
    }
}