package com.gumraze.rallyon.backend.identity.application.service;

import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthProviderRegistry;
import com.gumraze.rallyon.backend.identity.application.port.in.AuthenticateOAuthIdentityUseCase;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveAccountPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedAccount;
import com.gumraze.rallyon.backend.identity.domain.OAuthUserInfo;
import com.gumraze.rallyon.backend.identity.entity.Account;
import com.gumraze.rallyon.backend.identity.entity.OAuthLink;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OAuthIdentityAuthenticator implements AuthenticateOAuthIdentityUseCase {

    private final OAuthProviderRegistry oAuthProviderRegistry;
    private final OAuthAllowedProvidersProperties allowedProviders;
    private final LoadOAuthLinkPort loadOAuthLinkPort;
    private final SaveOAuthLinkPort saveOAuthLinkPort;
    private final SaveAccountPort saveAccountPort;

    public OAuthIdentityAuthenticator(
            OAuthProviderRegistry oAuthProviderRegistry,
            OAuthAllowedProvidersProperties allowedProviders,
            LoadOAuthLinkPort loadOAuthLinkPort,
            SaveOAuthLinkPort saveOAuthLinkPort,
            SaveAccountPort saveAccountPort
    ) {
        this.oAuthProviderRegistry = oAuthProviderRegistry;
        this.allowedProviders = allowedProviders;
        this.loadOAuthLinkPort = loadOAuthLinkPort;
        this.saveOAuthLinkPort = saveOAuthLinkPort;
        this.saveAccountPort = saveAccountPort;
    }

    @Override
    public AuthenticatedAccount authenticate(AuthProvider provider, String authorizationCode, String redirectUri) {
        validateAllowedProvider(provider);

        OAuthUserInfo userInfo = oAuthProviderRegistry.resolve(provider)
                .getOAuthUserInfo(authorizationCode, redirectUri);

        OAuthLink link = loadOAuthLinkPort.loadByProviderAndProviderUserId(provider, userInfo.providerUserId())
                .orElseGet(() -> createNewLink(provider, userInfo));

        link.applySnapshot(userInfo);
        OAuthLink savedLink = saveOAuthLinkPort.save(link);

        return new AuthenticatedAccount(
                savedLink.getAccount().getId(),
                savedLink.getAccount().getRole(),
                savedLink.getNickname()
        );
    }

    private void validateAllowedProvider(AuthProvider provider) {
        if (!allowedProviders.allowedProviders().contains(provider)) {
            throw new IllegalArgumentException("허용되지 않는 provider: " + provider);
        }
    }

    private OAuthLink createNewLink(AuthProvider provider, OAuthUserInfo userInfo) {
        Account account = saveAccountPort.save(Account.create());
        return OAuthLink.link(account, provider, userInfo.providerUserId());
    }
}
