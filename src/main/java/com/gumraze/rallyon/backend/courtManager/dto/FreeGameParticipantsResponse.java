package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class FreeGameParticipantsResponse {
    private UUID gameId;
    private MatchRecordMode matchRecordMode;
    private List<FreeGameParticipantResponse> participants;
}
