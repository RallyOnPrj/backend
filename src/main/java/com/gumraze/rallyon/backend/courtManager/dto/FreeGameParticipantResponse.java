package com.gumraze.rallyon.backend.courtManager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;

import java.util.UUID;

public record FreeGameParticipantResponse(
        UUID participantId,
        UUID accountId,
        String displayName,
        Gender gender,
        Grade grade,
        Integer ageGroup,
        @JsonInclude(JsonInclude.Include.NON_NULL) Integer assignedMatchCount,
        @JsonInclude(JsonInclude.Include.NON_NULL) Integer completedMatchCount,
        @JsonInclude(JsonInclude.Include.NON_NULL) Integer winCount,
        @JsonInclude(JsonInclude.Include.NON_NULL) Integer lossCount
) {
}
