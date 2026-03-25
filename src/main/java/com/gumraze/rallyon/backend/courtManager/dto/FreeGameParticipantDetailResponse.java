package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;

import java.util.UUID;

public record FreeGameParticipantDetailResponse(
        UUID gameId,
        UUID participantId,
        UUID identityAccountId,
        String displayName,
        Gender gender,
        Grade grade,
        Integer ageGroup
) {
}
