package com.gumraze.rallyon.backend.identity.adapter.out.oauth.apple;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "oauth.apple")
public record AppleOAuthProperties(
        boolean enabled,
        String clientId,
        String teamId,
        String keyId,
        String privateKey,
        String authorizationUri,
        String tokenUri,
        List<String> scopes
) {
}
