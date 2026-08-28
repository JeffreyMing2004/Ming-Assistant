package com.ming.server.gift;

import com.ming.server.config.ApiException;
import com.ming.server.gift.dto.GiftRequest;
import com.ming.server.user.User;
import com.ming.server.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GiftService {

    private final GiftRepository giftRepository;
    private final UserRepository userRepository;

    @Transactional
    public GiftRecord create(Long userId, GiftRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("用户不存在"));
        if (user.getBilibiliUid() == null || user.getBilibiliUid().isBlank()) {
            throw ApiException.badRequest("您注册时未填写本人B站UID，暂无法登记舰礼。请重新注册并填写B站UID后再登记。");
        }
        GiftRecord record = new GiftRecord();
        record.setUserId(userId);
        record.setNickname(req.getNickname());
        record.setBilibiliUid(req.getBilibiliUid());
        record.setPhone(req.getPhone());
        record.setAddress(req.getAddress());
        record.setGiftType(req.getGiftType());
        return giftRepository.save(record);
    }

    public List<GiftRecord> list(Long userId) {
        return giftRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        if (!giftRepository.existsByIdAndUserId(id, userId)) {
            throw ApiException.notFound("舰礼记录不存在");
        }
        giftRepository.deleteById(id);
    }
}