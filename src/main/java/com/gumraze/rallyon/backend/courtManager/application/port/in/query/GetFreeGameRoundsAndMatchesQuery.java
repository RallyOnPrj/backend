package com.gumraze.rallyon.backend.courtManager.application.port.in.query;

import java.util.UUID;

public record GetFreeGameRoundsAndMatchesQuery(
        UUID organizerId,
        UUID gameId
) {
}
