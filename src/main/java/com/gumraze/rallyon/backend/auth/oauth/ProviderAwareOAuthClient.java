package com.gumraze.rallyon.backend.auth.oauth;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;

public interface ProviderAwareOAuthClient {
    AuthProvider supports();
}
