package com.gumraze.rallyon.backend.auth.oauth;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
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