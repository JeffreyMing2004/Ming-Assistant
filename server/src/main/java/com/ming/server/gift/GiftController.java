package com.ming.server.gift;

import com.ming.server.gift.dto.GiftRequest;
import com.ming.server.security.AppPrincipal;
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
@RequestMapping("/api/gifts")
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;

    @PostMapping
    public GiftRecord create(@AuthenticationPrincipal AppPrincipal principal,
                             @Valid @RequestBody GiftRequest request) {
        return giftService.create(principal.userId(), request);
    }

    @GetMapping
    public List<GiftRecord> list(@AuthenticationPrincipal AppPrincipal principal) {
        return giftService.list(principal.userId());
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal AppPrincipal principal, @PathVariable Long id) {
        giftService.delete(principal.userId(), id);
    }
}