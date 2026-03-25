package com.gumraze.rallyon.backend.identity.authorizationserver.domain;

import java.io.Serializable;

public record BrowserAuthorizationRequestContext(
        String authorizationState,
        String socialState,
        String codeVerifier,
        String returnTo,
        String screen
) implements Serializable {
}
