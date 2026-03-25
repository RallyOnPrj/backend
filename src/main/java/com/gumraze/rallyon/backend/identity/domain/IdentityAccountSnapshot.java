package com.gumraze.rallyon.backend.identity.domain;

import com.gumraze.rallyon.backend.user.constants.UserStatus;

import java.util.UUID;

public record IdentityAccountSnapshot(
        UUID userId,
        UserStatus status
) {
}
