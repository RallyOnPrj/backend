package com.gumraze.rallyon.backend.identity.application.port.in;

import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedIdentity;

public interface AuthenticateOAuthIdentityUseCase {

    AuthenticatedIdentity authenticate(AuthProvider provider, String authorizationCode, String redirectUri);
}
