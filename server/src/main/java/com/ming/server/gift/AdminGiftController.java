package com.ming.server.gift;

import com.ming.server.config.ApiException;
import com.ming.server.gift.dto.AdminGiftView;
import com.ming.server.gift.dto.GiftRequest;
import com.ming.server.gift.dto.TrackingRequest;
import com.ming.server.security.AppPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站长后台舰礼管理：查看全部用户提交、手动登记、删除、登记快递单号。
 * 仅 Admin 身份（admin_users 表账号）可访问。
 */
@RestController
@RequestMapping("/api/admin/gifts")
@RequiredArgsConstructor
public class AdminGiftController {

    private final GiftService giftService;

    @GetMapping
    public List<AdminGiftView> list(@AuthenticationPrincipal AppPrincipal principal) {
        requireAdmin(principal);
        return giftService.listAll();
    }

    /** 后台手动登记一条舰礼（归属站长账号名下）。 */
    @PostMapping
    public GiftRecord create(@AuthenticationPrincipal AppPrincipal principal,
                             @Valid @RequestBody GiftRequest request) {
        requireAdmin(principal);
        return giftService.create(principal.userId(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AppPrincipal principal, @PathVariable Long id) {
        requireAdmin(principal);
        giftService.deleteById(id);
    }

    /** 登记 / 修改该条舰礼的快递单号，方便用户在 App 查询物流。 */
    @PostMapping("/{id}/tracking")
    public GiftRecord setTracking(@AuthenticationPrincipal AppPrincipal principal,
                                  @PathVariable Long id,
                                  @Valid @RequestBody TrackingRequest request) {
        requireAdmin(principal);
        return giftService.setTracking(id, request.getTrackingNumber());
    }

    private void requireAdmin(AppPrincipal principal) {
        if (principal == null || !principal.admin()) {
            throw ApiException.forbidden("仅站长后台管理员可操作");
        }
    }
}