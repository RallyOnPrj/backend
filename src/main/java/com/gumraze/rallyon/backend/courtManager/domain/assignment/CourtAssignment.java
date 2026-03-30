package com.gumraze.rallyon.backend.courtManager.domain.assignment;

import java.util.UUID;

public record CourtAssignment(
        Integer courtNumber,
        UUID slot1ParticipantId,
        UUID slot2ParticipantId,
        UUID slot3ParticipantId,
        UUID slot4ParticipantId
) {
}
