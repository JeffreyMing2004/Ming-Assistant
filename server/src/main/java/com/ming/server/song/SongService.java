package com.ming.server.song;

import com.ming.server.config.ApiException;
import com.ming.server.song.dto.SongRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    @Transactional
    public Song create(Long userId, SongRequest req) {
        Song song = new Song();
        song.setUserId(userId);
        song.setTitle(req.getTitle());
        song.setArtist(req.getArtist());
        song.setNote(req.getNote());
        return songRepository.save(song);
    }

    public List<Song> list(Long userId) {
        return songRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        if (!songRepository.existsByIdAndUserId(id, userId)) {
            throw ApiException.notFound("歌单记录不存在");
        }
        songRepository.deleteById(id);
    }
}