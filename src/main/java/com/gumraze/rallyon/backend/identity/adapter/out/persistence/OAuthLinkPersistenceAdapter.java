package com.gumraze.rallyon.backend.identity.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.OAuthLinkRepository;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.entity.OAuthLink;
import com.gumraze.rallyon.backend.user.application.port.out.LoadAccountDisplayNamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuthLinkPersistenceAdapter implements LoadOAuthLinkPort, SaveOAuthLinkPort, LoadAccountDisplayNamePort {

    private final OAuthLinkRepository repository;

    @Override
    public Optional<OAuthLink> loadByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return repository.findByProviderAndProviderUserId(provider, providerUserId);
    }

    @Override
    public Optional<OAuthLink> loadByAccountIdAndProvider(UUID accountId, AuthProvider provider) {
        return repository.findByAccount_IdAndProvider(accountId, provider);
    }

    @Override
    public Optional<String> loadLatestDisplayName(UUID accountId) {
        return repository.findByAccount_IdOrderByUpdatedAtDesc(accountId).stream()
                .map(OAuthLink::getNickname)
                .filter(nickname -> nickname != null && !nickname.isBlank())
                .findFirst();
    }

    @Override
    public OAuthLink save(OAuthLink oauthLink) {
        return repository.save(oauthLink);
    }
}
