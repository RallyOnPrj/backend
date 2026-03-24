package com.gumraze.rallyon.backend.identity.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityOAuthLinkRepository;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.domain.authentication.AuthProvider;
import com.gumraze.rallyon.backend.identity.entity.IdentityOAuthLink;
import com.gumraze.rallyon.backend.user.application.port.out.LoadIdentityDisplayNamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdentityOAuthLinkPersistenceAdapter implements LoadOAuthLinkPort, SaveOAuthLinkPort, LoadIdentityDisplayNamePort {

    private final IdentityOAuthLinkRepository repository;

    @Override
    public Optional<IdentityOAuthLink> loadByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return repository.findByProviderAndProviderUserId(provider, providerUserId);
    }

    @Override
    public Optional<IdentityOAuthLink> loadByUserIdAndProvider(UUID userId, AuthProvider provider) {
        return repository.findByUser_IdAndProvider(userId, provider);
    }

    @Override
    public Optional<String> loadLatestDisplayName(UUID userId) {
        return repository.findByUser_IdOrderByUpdatedAtDesc(userId).stream()
                .map(IdentityOAuthLink::getNickname)
                .filter(nickname -> nickname != null && !nickname.isBlank())
                .findFirst();
    }

    @Override
    public IdentityOAuthLink save(IdentityOAuthLink identityOAuthLink) {
        return repository.save(identityOAuthLink);
    }
}
