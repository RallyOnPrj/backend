package com.gumraze.rallyon.backend.identity.adapter.out.oauth;

import com.gumraze.rallyon.backend.identity.domain.authentication.AuthProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth")
public class OAuthAllowedProvidersProperties {
    private List<AuthProvider> allowedProviders = List.of(AuthProvider.KAKAO);
}
