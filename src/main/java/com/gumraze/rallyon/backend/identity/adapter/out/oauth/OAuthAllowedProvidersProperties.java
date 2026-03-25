package com.gumraze.rallyon.backend.identity.adapter.out.oauth;

import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "oauth")
public record OAuthAllowedProvidersProperties(List<AuthProvider> allowedProviders) {

    public OAuthAllowedProvidersProperties {
        allowedProviders = allowedProviders == null
                ? List.of(AuthProvider.KAKAO)
                : List.copyOf(allowedProviders);
    }
}
