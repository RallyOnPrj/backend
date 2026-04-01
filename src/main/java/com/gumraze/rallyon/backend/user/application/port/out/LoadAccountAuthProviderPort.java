package com.gumraze.rallyon.backend.user.application.port.out;

import com.gumraze.rallyon.backend.identity.domain.AuthProvider;

import java.util.Optional;
import java.util.UUID;

public interface LoadAccountAuthProviderPort {
    Optional<AuthProvider> loadLatestAuthProvider(UUID accountId);
}
