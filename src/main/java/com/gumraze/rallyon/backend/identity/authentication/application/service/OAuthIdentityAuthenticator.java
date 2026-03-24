package com.gumraze.rallyon.backend.identity.authentication.application.service;

import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthProviderRegistry;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveIdentityAccountPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.IdentityAuthenticatedPrincipal;
import com.gumraze.rallyon.backend.identity.domain.authentication.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.authentication.OAuthUserInfo;
import com.gumraze.rallyon.backend.identity.entity.IdentityOAuthLink;
import com.gumraze.rallyon.backend.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OAuthIdentityAuthenticator {

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

    public IdentityAuthenticatedPrincipal authenticate(AuthProvider provider, String authorizationCode, String redirectUri) {
        validateAllowedProvider(provider);

        OAuthUserInfo userInfo = oAuthProviderRegistry.resolve(provider)
                .getOAuthUserInfo(authorizationCode, redirectUri);

        IdentityOAuthLink link = loadOAuthLinkPort.loadByProviderAndProviderUserId(provider, userInfo.getProviderUserId())
                .orElseGet(() -> createNewLink(provider, userInfo));

        applySnapshot(link, userInfo);
        IdentityOAuthLink savedLink = saveOAuthLinkPort.save(link);

        return new IdentityAuthenticatedPrincipal(
                savedLink.getUser().getId(),
                savedLink.getUser().getRole(),
                savedLink.getUser().getStatus(),
                savedLink.getNickname()
        );
    }

    private void validateAllowedProvider(AuthProvider provider) {
        if (!allowedProviders.getAllowedProviders().contains(provider)) {
            throw new IllegalArgumentException("허용되지 않는 provider: " + provider);
        }
    }

    private IdentityOAuthLink createNewLink(AuthProvider provider, OAuthUserInfo userInfo) {
        User user = saveIdentityAccountPort.save(User.builder().build());
        return IdentityOAuthLink.builder()
                .user(user)
                .provider(provider)
                .providerUserId(userInfo.getProviderUserId())
                .build();
    }

    private void applySnapshot(IdentityOAuthLink link, OAuthUserInfo userInfo) {
        link.setEmail(userInfo.getEmail());
        link.setNickname(userInfo.getNickname());
        link.setProfileImageUrl(userInfo.getProfileImageUrl());
        link.setThumbnailImageUrl(userInfo.getThumbnailImageUrl());
        link.setGender(userInfo.getGender());
        link.setAgeRange(userInfo.getAgeRange());
        link.setBirthday(userInfo.getBirthday());
        link.setEmailVerified(userInfo.getEmailVerified());
        link.setPhoneNumberVerified(userInfo.getPhoneNumberVerified());
    }
}
