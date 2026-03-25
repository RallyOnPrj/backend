package com.gumraze.rallyon.backend.identity.application.service;

import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthProviderRegistry;
import com.gumraze.rallyon.backend.identity.application.port.in.AuthenticateOAuthIdentityUseCase;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveIdentityAccountPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedIdentity;
import com.gumraze.rallyon.backend.identity.domain.OAuthUserInfo;
import com.gumraze.rallyon.backend.identity.entity.IdentityAccount;
import com.gumraze.rallyon.backend.identity.entity.IdentityOAuthLink;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OAuthIdentityAuthenticator implements AuthenticateOAuthIdentityUseCase {

    private final OAuthProviderRegistry oAuthProviderRegistry;
    private final OAuthAllowedProvidersProperties allowedProviders;
    private final LoadOAuthLinkPort loadOAuthLinkPort;
    private final SaveOAuthLinkPort saveOAuthLinkPort;
    private final SaveIdentityAccountPort saveIdentityAccountPort;

    public OAuthIdentityAuthenticator(
            OAuthProviderRegistry oAuthProviderRegistry,
            OAuthAllowedProvidersProperties allowedProviders,
            LoadOAuthLinkPort loadOAuthLinkPort,
            SaveOAuthLinkPort saveOAuthLinkPort,
            SaveIdentityAccountPort saveIdentityAccountPort
    ) {
        this.oAuthProviderRegistry = oAuthProviderRegistry;
        this.allowedProviders = allowedProviders;
        this.loadOAuthLinkPort = loadOAuthLinkPort;
        this.saveOAuthLinkPort = saveOAuthLinkPort;
        this.saveIdentityAccountPort = saveIdentityAccountPort;
    }

    @Override
    public AuthenticatedIdentity authenticate(AuthProvider provider, String authorizationCode, String redirectUri) {
        validateAllowedProvider(provider);

        OAuthUserInfo userInfo = oAuthProviderRegistry.resolve(provider)
                .getOAuthUserInfo(authorizationCode, redirectUri);

        IdentityOAuthLink link = loadOAuthLinkPort.loadByProviderAndProviderUserId(provider, userInfo.providerUserId())
                .orElseGet(() -> createNewLink(provider, userInfo));

        link.applySnapshot(userInfo);
        IdentityOAuthLink savedLink = saveOAuthLinkPort.save(link);

        return new AuthenticatedIdentity(
                savedLink.getIdentityAccount().getId(),
                savedLink.getIdentityAccount().getRole(),
                savedLink.getNickname()
        );
    }

    private void validateAllowedProvider(AuthProvider provider) {
        if (!allowedProviders.allowedProviders().contains(provider)) {
            throw new IllegalArgumentException("허용되지 않는 provider: " + provider);
        }
    }

    private IdentityOAuthLink createNewLink(AuthProvider provider, OAuthUserInfo userInfo) {
        IdentityAccount identityAccount = saveIdentityAccountPort.save(IdentityAccount.create());
        return IdentityOAuthLink.link(identityAccount, provider, userInfo.providerUserId());
    }
}
