package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.entity.IdentityOAuthLink;

import java.util.Optional;
import java.util.UUID;

public interface LoadOAuthLinkPort {

    Optional<IdentityOAuthLink> loadByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<IdentityOAuthLink> loadByUserIdAndProvider(UUID userId, AuthProvider provider);

    Optional<String> loadLatestDisplayName(UUID userId);
}
