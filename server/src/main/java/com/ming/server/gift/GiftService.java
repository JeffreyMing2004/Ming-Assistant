package com.ming.server.gift;

import com.ming.server.config.ApiException;
import com.ming.server.gift.dto.AdminGiftView;
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

    /* ---------- 以下为站长后台管理操作 ---------- */

    /** 后台查看全部用户提交的舰礼（含提交人用户名）。 */
    public List<AdminGiftView> listAll() {
        return giftRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(g -> {
                    String username = userRepository.findById(g.getUserId())
                            .map(User::getUsername)
                            .orElse("—");
                    return AdminGiftView.from(g, username);
                })
                .toList();
    }

    /** 后台删除任意一条舰礼记录。 */
    @Transactional
    public void deleteById(Long id) {
        if (!giftRepository.existsById(id)) {
            throw ApiException.notFound("舰礼记录不存在");
        }
        giftRepository.deleteById(id);
    }

    /** 后台登记 / 修改该条舰礼的快递单号（用户可在 App 查询物流）。 */
    @Transactional
    public GiftRecord setTracking(Long id, String trackingNumber) {
        GiftRecord record = giftRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("舰礼记录不存在"));
        record.setTrackingNumber(trackingNumber == null ? null : trackingNumber.trim());
        return giftRepository.save(record);
    }
}