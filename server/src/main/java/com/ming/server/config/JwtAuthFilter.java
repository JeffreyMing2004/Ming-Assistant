package com.ming.server.config;

import com.ming.server.admin.AdminUserRepository;
import com.ming.server.security.AppPrincipal;
import com.ming.server.security.JwtService;
import com.ming.server.security.JwtService.TokenClaims;
import com.ming.server.user.User;
import com.ming.server.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            TokenClaims claims = jwtService.parse(header.substring(7));
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateByClaims(claims, request);
            }
        } catch (RuntimeException ignored) {
            // Invalid/expired token: leave the user unauthenticated.
        }
        filterChain.doFilter(request, response);
    }

    private void authenticateByClaims(TokenClaims claims, HttpServletRequest request) {
        if (JwtService.TYPE_ADMIN.equals(claims.type())) {
            // 管理员 token：需管理员账号仍存在，并以站长用户身份行使操作
            if (!adminUserRepository.existsByUsername(claims.subject())) {
                return;
            }
            userRepository.findById(claims.uid()).ifPresent(owner ->
                    authenticate(owner, true, request));
        } else {
            userRepository.findById(claims.uid()).ifPresent(user ->
                    authenticate(user, false, request));
        }
    }

    private void authenticate(User user, boolean admin, HttpServletRequest request) {
        AppPrincipal principal = new AppPrincipal(user.getId(), user.getUsername(), admin);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}