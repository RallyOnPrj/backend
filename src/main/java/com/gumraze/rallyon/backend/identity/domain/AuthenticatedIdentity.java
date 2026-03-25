package com.gumraze.rallyon.backend.identity.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthenticatedIdentity(
        UUID identityAccountId,
        IdentityRole role,
        String displayName
) implements Principal, Serializable {

    @Override
    public String getName() {
        return identityAccountId.toString();
    }
}
