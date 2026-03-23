package com.gumraze.rallyon.backend.courtManager.application.port.in.query;

import java.util.UUID;

public record GetFreeGameDetailQuery(
        UUID organizerId,
        UUID gameId
) {
}
