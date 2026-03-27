package com.gumraze.rallyon.backend.identity.application.port.in;

import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedAccount;

public interface AuthenticateOAuthIdentityUseCase {

    AuthenticatedAccount authenticate(AuthProvider provider, String authorizationCode, String redirectUri);
}
