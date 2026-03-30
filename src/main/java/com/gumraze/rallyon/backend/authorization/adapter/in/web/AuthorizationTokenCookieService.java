package com.gumraze.rallyon.backend.authorization.adapter.in.web;

import com.gumraze.rallyon.backend.authorization.config.AuthorizationProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthorizationTokenCookieService {

    private final AuthorizationProperties properties;

    public AuthorizationTokenCookieService(AuthorizationProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie buildAccessTokenCookie(String accessToken) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getCookies().getAccessTokenName(), accessToken)
                .httpOnly(true)
                .secure(properties.getCookies().isSecure())
                .path(properties.getCookies().getAccessTokenPath())
                .sameSite(properties.getCookies().getSameSite())
                .maxAge(Duration.ofSeconds(properties.getTokens().getAccessTokenExpirationSeconds()));

        if (hasText(properties.getCookies().getAccessTokenDomain())) {
            builder.domain(properties.getCookies().getAccessTokenDomain());
        }

        return builder.build();
    }

    public ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getCookies().getRefreshTokenName(), refreshToken)
                .httpOnly(true)
                .secure(properties.getCookies().isSecure())
                .path(properties.getCookies().getRefreshTokenPath())
                .sameSite(properties.getCookies().getSameSite())
                .maxAge(Duration.ofSeconds(properties.getTokens().getRefreshTokenExpirationSeconds()));

        if (hasText(properties.getCookies().getRefreshTokenDomain())) {
            builder.domain(properties.getCookies().getRefreshTokenDomain());
        }

        return builder.build();
    }

    public ResponseCookie expireAccessTokenCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getCookies().getAccessTokenName(), "")
                .httpOnly(true)
                .secure(properties.getCookies().isSecure())
                .path(properties.getCookies().getAccessTokenPath())
                .sameSite(properties.getCookies().getSameSite())
                .maxAge(0);

        if (hasText(properties.getCookies().getAccessTokenDomain())) {
            builder.domain(properties.getCookies().getAccessTokenDomain());
        }

        return builder.build();
    }

    public ResponseCookie expireRefreshTokenCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getCookies().getRefreshTokenName(), "")
                .httpOnly(true)
                .secure(properties.getCookies().isSecure())
                .path(properties.getCookies().getRefreshTokenPath())
                .sameSite(properties.getCookies().getSameSite())
                .maxAge(0);

        if (hasText(properties.getCookies().getRefreshTokenDomain())) {
            builder.domain(properties.getCookies().getRefreshTokenDomain());
        }

        return builder.build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
