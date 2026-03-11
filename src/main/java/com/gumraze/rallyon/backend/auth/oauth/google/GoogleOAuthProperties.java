package com.gumraze.rallyon.backend.auth.oauth.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.google")
public record GoogleOAuthProperties(
        boolean enabled,
        String clientId,
        String clientSecret,
        String tokenUri,
        String userInfoUri
){
}
