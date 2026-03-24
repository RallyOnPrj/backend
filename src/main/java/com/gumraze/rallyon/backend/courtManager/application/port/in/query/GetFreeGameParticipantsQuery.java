package com.gumraze.rallyon.backend.courtManager.application.port.in.query;

import java.util.UUID;

public record GetFreeGameParticipantsQuery(
        UUID organizerId,
        UUID gameId,
        boolean includeStats
) {
}
