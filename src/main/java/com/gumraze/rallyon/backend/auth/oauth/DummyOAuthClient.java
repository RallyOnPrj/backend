package com.gumraze.rallyon.backend.auth.oauth;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "oauth.dummy", name = "enabled", havingValue = "true")
public class DummyOAuthClient implements OAuthClient, ProviderAwareOAuthClient {
    @Override
    public AuthProvider supports() {
        return AuthProvider.DUMMY;
    }

    @Override
    public OAuthUserInfo getOAuthUserInfo(
            String authorizationCode,
            String redirectUri
    ) {
        String providerUserId = (authorizationCode == null || authorizationCode.isBlank())
                ? "dummy"
                : authorizationCode;

        return OAuthUserInfo.builder()
                .providerUserId(providerUserId)
                .email(authorizationCode + "@dummy.com")
                .nickname(providerUserId)
                .build();
    }
}
