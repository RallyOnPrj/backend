package com.gumraze.rallyon.backend.identity.authorizationserver.domain;

import com.gumraze.rallyon.backend.user.constants.UserRole;
import com.gumraze.rallyon.backend.user.constants.UserStatus;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

public record IdentityAuthenticatedPrincipal(
        UUID userId,
        UserRole role,
        UserStatus status,
        String displayName
) implements Principal, Serializable {

    @Override
    public String getName() {
        return userId.toString();
    }
}
