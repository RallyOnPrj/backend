package com.gumraze.rallyon.backend.identity.adapter.out.oauth.dummy;

import com.gumraze.rallyon.backend.identity.application.port.out.OAuthProviderPort;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.OAuthUserInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "oauth.dummy", name = "enabled", havingValue = "true")
public class DummyOAuthClient implements OAuthProviderPort {
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
