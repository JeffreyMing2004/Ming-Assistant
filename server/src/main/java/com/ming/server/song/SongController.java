package com.ming.server.song;

import com.ming.server.security.AppPrincipal;
import com.ming.server.song.dto.SongRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    public Song create(@AuthenticationPrincipal AppPrincipal principal,
                       @Valid @RequestBody SongRequest request) {
        return songService.create(principal.userId(), request);
    }

    @GetMapping
    public List<Song> list(@AuthenticationPrincipal AppPrincipal principal) {
        return songService.list(principal.userId());
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal AppPrincipal principal, @PathVariable Long id) {
        songService.delete(principal.userId(), id);
    }
}