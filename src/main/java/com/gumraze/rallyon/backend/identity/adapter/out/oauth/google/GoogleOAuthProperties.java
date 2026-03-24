package com.gumraze.rallyon.backend.identity.adapter.out.oauth.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "oauth.google")
public record GoogleOAuthProperties(
        boolean enabled,
        String clientId,
        String clientSecret,
        String authorizationUri,
        String tokenUri,
        String userInfoUri,
        List<String> scopes
){
}
