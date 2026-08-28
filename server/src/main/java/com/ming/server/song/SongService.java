package com.ming.server.song;

import com.ming.server.config.ApiException;
import com.ming.server.song.dto.SongRequest;
import com.ming.server.user.User;
import com.ming.server.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final UserRepository userRepository;

    @Value("${app.qq.owner-username:testuser}")
    private String ownerUsername;

    /**
     * 直播歌单 = 站长（owner）提供的歌单，对所有登录用户共享展示。
     * 新增 / 删除仅站长可操作，其他人只读。
     */
    @Transactional
    public Song create(Long userId, SongRequest req) {
        requireOwner(userId);
        Song song = new Song();
        song.setUserId(ownerId());
        song.setTitle(req.getTitle());
        song.setArtist(req.getArtist());
        song.setNote(req.getNote());
        return songRepository.save(song);
    }

    public List<Song> list() {
        return songRepository.findByUserIdOrderByCreatedAtDesc(ownerId());
    }

    @Transactional
    public void delete(Long userId, Long id) {
        requireOwner(userId);
        if (!songRepository.existsByIdAndUserId(id, ownerId())) {
            throw ApiException.notFound("歌单记录不存在");
        }
        songRepository.deleteById(id);
    }

    private void requireOwner(Long userId) {
        if (!ownerId().equals(userId)) {
            throw ApiException.forbidden("只有站长可以管理直播歌单，其他账号仅可查看");
        }
    }

    private Long ownerId() {
        return userRepository.findByUsername(ownerUsername)
                .map(User::getId)
                .orElseThrow(() -> ApiException.badRequest(
                        "未配置直播歌单账号（app.qq.owner-username，当前为 " + ownerUsername + "）"));
    }
}