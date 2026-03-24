package com.gumraze.rallyon.backend.identity.adapter.out.oauth.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOAuthProperties(
        String clientId,
        String clientSecret,
        String authorizationUri,
        String tokenUri,
        String userInfoUri,
        List<String> scopes
) {
}
