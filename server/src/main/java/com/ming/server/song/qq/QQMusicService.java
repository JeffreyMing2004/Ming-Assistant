package com.ming.server.song.qq;

import com.ming.server.song.Song;
import com.ming.server.song.SongRepository;
import com.ming.server.song.qq.dto.QqImportResult;
import com.ming.server.song.qq.dto.QqPreview;
import com.ming.server.song.qq.dto.QqTrack;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QQMusicService {

    private final QQMusicClient qqMusicClient;
    private final SongRepository songRepository;

    /** 预览：抓取歌单并统计与现有歌单的重复数（不写入）。 */
    public QqPreview preview(Long userId, String url) {
        QQMusicClient.QqPlaylistData data = qqMusicClient.fetch(url);
        Set<String> mine = keys(userId);
        int duplicate = 0;
        for (QqTrack t : data.tracks()) {
            if (mine.contains(key(t.title(), t.artist()))) {
                duplicate++;
            }
        }
        return new QqPreview(data.title(), data.total(), duplicate, data.tracks());
    }

    /** 导入：把歌单中未重复的歌曲写入歌单，备注来源。 */
    @Transactional
    public QqImportResult importPlaylist(Long userId, String url) {
        QQMusicClient.QqPlaylistData data = qqMusicClient.fetch(url);
        Set<String> mine = keys(userId);
        int imported = 0;
        int skipped = 0;
        for (QqTrack t : data.tracks()) {
            String k = key(t.title(), t.artist());
            if (!mine.add(k)) {
                skipped++;
                continue;
            }
            Song song = new Song();
            song.setUserId(userId);
            song.setTitle(t.title());
            song.setArtist(t.artist());
            song.setNote("来自QQ音乐歌单：《" + data.title() + "》");
            songRepository.save(song);
            imported++;
        }
        return new QqImportResult(imported, skipped, data.title());
    }

    private Set<String> keys(Long userId) {
        return songRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(s -> key(s.getTitle(), s.getArtist()))
                .collect(Collectors.toSet());
    }

    private static String key(String title, String artist) {
        return (title == null ? "" : title.trim().toLowerCase())
                + "\u0001"
                + (artist == null ? "" : artist.trim().toLowerCase());
    }
}