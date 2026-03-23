package com.gumraze.rallyon.backend.user.application.port.in.command;

import java.util.UUID;

public record UpdateMyPublicIdentityCommand(
        UUID userId,
        String nickname,
        String tag
) {
}
