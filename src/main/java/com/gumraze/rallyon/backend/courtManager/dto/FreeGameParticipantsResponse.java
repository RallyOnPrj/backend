package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;

import java.util.List;
import java.util.UUID;

public record FreeGameParticipantsResponse(
        UUID gameId,
        MatchRecordMode matchRecordMode,
        List<FreeGameParticipantResponse> participants
) {}
