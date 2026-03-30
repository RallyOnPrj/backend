package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.entity.OAuthLink;

import java.util.Optional;
import java.util.UUID;

public interface LoadOAuthLinkPort {

    Optional<OAuthLink> loadByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<OAuthLink> loadByAccountIdAndProvider(UUID accountId, AuthProvider provider);

    Optional<String> loadLatestDisplayName(UUID accountId);
}
