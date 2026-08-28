package com.ming.server.gift;

import com.ming.server.config.ApiException;
import com.ming.server.gift.dto.GiftRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GiftService {

    private final GiftRepository giftRepository;

    @Transactional
    public GiftRecord create(Long userId, GiftRequest req) {
        GiftRecord record = new GiftRecord();
        record.setUserId(userId);
        record.setNickname(req.getNickname());
        record.setBilibiliUid(req.getBilibiliUid());
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