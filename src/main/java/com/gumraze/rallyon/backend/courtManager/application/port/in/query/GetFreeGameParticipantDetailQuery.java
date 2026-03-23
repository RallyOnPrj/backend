package com.gumraze.rallyon.backend.courtManager.application.port.in.query;

import java.util.UUID;

public record GetFreeGameParticipantDetailQuery(
        UUID organizerId,
        UUID gameId,
        UUID participantId
) {
}
