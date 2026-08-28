package com.ming.server.auth;

import com.ming.server.auth.dto.AuthResponse;
import com.ming.server.auth.dto.LoginRequest;
import com.ming.server.auth.dto.RegisterRequest;
import com.ming.server.auth.dto.UpdateUidRequest;
import com.ming.server.security.AppPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal AppPrincipal principal) {
        return authService.me(principal.userId());
    }

    @PutMapping("/me")
    public AuthResponse updateBilibiliUid(@Valid @RequestBody UpdateUidRequest request,
                                          @AuthenticationPrincipal AppPrincipal principal) {
        return authService.updateBilibiliUid(principal.userId(), request.getBilibiliUid());
    }

    @DeleteMapping("/account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@AuthenticationPrincipal AppPrincipal principal) {
        authService.deleteAccount(principal.userId());
    }
}