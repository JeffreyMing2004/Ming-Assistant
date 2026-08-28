package com.ming.server.song;

import com.ming.server.song.dto.SongRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    public Song create(@Valid @RequestBody SongRequest request) {
        return songService.create(request);
    }

    @GetMapping
    public List<Song> list() {
        return songService.list();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        songService.delete(id);
    }
}