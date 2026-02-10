package com.gumraze.drive.drive_backend.auth.controller;

import com.gumraze.drive.drive_backend.api.auth.AuthApi;
import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.service.AuthService;
import com.gumraze.drive.drive_backend.auth.service.OAuthLoginResult;
import com.gumraze.drive.drive_backend.auth.token.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * OAuth 인증 플로우의 진입점 컨트롤러.
 *
 * <p>로그인/리프레시 시 Access Token과 Refresh Token을 HttpOnly, Secure 쿠키로 발급하고,
 * 로그아웃 시 두 쿠키를 만료 처리한다.</p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final AuthService authService;
    private final JwtProperties properties;

    /**
     * OAuth 로그인 후 액세스/리프레시 토큰 쿠키를 발급한다.
     *
     * @param request OAuth 공급자 로그인 요청 정보
     * @return 204 No Content + Set-Cookie(access_token, refresh_token)
     */
    @Override
    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody OAuthLoginRequestDto request
    ) {
        OAuthLoginResult result = authService.login(request);

        ResponseCookie accessTokenCookie = buildAccessTokenCookie(result.accessToken());
        ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(result.refreshToken());

        return ResponseEntity
                .noContent()
                .headers(headers -> {
                    headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
                    headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
                })
                .build();
    }

    /**
     * refresh_token 쿠키를 검증하여 새 액세스/리프레시 토큰을 재발급한다.
     *
     * @param refreshToken 요청 쿠키의 refresh_token 값
     * @return 204 No Content + Set-Cookie(access_token, refresh_token)
     */
    @Override
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh Token이 없습니다.");
        }

        OAuthLoginResult result = authService.refresh(refreshToken);

        ResponseCookie accessTokenCookie = buildAccessTokenCookie(result.accessToken());
        ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(result.refreshToken());

        return ResponseEntity
                .noContent()
                .headers(headers -> {
                    headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
                    headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
                })
                .build();
    }

    /**
     * refresh_token을 서버 저장소에서 제거하고 토큰 쿠키를 만료 처리한다.
     *
     * @param refreshToken 요청 쿠키의 refresh_token 값
     * @return 204 No Content + 만료된 Set-Cookie(access_token, refresh_token)
     */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        ResponseCookie expiredAccessCookie = expireAccessTokenCookie();
        ResponseCookie expiredRefreshCookie = expireRefreshTokenCookie();

        return ResponseEntity.noContent()
                .headers(headers -> {
                    headers.add(HttpHeaders.SET_COOKIE, expiredAccessCookie.toString());
                    headers.add(HttpHeaders.SET_COOKIE, expiredRefreshCookie.toString());
                })
                .build();
    }

    /**
     * 액세스 토큰 쿠키를 생성한다.
     * 쿠키 경로는 애플리케이션 전체 인증 요청을 위해 {@code /}를 사용한다.
     */
    private ResponseCookie buildAccessTokenCookie(String accessToken) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(properties.accessToken().expirationMs()))
                .sameSite("Strict")
                .build();
    }

    /**
     * 리프레시 토큰 쿠키를 생성한다.
     * 리프레시 엔드포인트만 필요하므로 쿠키 경로는 {@code /auth}를 사용한다.
     */
    private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(Duration.ofHours(properties.refreshToken().expirationHours()))
                .sameSite("Strict")
                .build();
    }

    /** access_token 쿠키를 즉시 만료시키기 위한 빈 쿠키를 생성한다. */
    private ResponseCookie expireAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    /** refresh_token 쿠키를 즉시 만료시키기 위한 빈 쿠키를 생성한다. */
    private ResponseCookie expireRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }
}
