package com.gumraze.rallyon.backend.auth.security;

import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 요청마다 액세스 토큰을 추출/검증해 SecurityContext에 인증 정보를 설정하는 필터.
 *
 * <p>토큰은 {@code access_token} 쿠키에서만 추출하며, 유효한 토큰이 없으면 인증 없이 다음
 * 필터로 진행한다.</p>
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final JwtAccessTokenValidator jwtAccessTokenValidator;

    /**
     * 요청에서 토큰을 추출해 검증하고, 성공 시 인증 객체를 SecurityContext에 저장한다.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            jwtAccessTokenValidator
                    .validateAndGetUserId(token)
                    .ifPresent(userId -> {
                        var authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userId,
                                        null,
                                        List.of(() -> "ROLE_USER")
                                );
                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    });
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 액세스 토큰을 추출한다.
     *
     * <p>{@code access_token} 쿠키만 사용한다.</p>
     *
     * @param request 현재 HTTP 요청
     * @return 추출된 토큰 문자열, 없으면 {@code null}
     */
    private String resolveToken(
            HttpServletRequest request
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())
                        && cookie.getValue() != null
                        && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
