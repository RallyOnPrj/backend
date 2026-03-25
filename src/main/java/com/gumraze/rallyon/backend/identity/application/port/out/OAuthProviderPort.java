package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.OAuthUserInfo;

public interface OAuthProviderPort {

    AuthProvider supports();

    OAuthUserInfo getOAuthUserInfo(String authorizationCode, String redirectUri);
}
