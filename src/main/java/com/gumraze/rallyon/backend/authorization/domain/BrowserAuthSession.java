package com.gumraze.rallyon.backend.authorization.domain;

import java.io.Serializable;

public record BrowserAuthSession(
        String authorizationState,
        String socialState,
        String codeVerifier,
        String returnTo,
        String screen
) implements Serializable {
}
