package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FreeGameParticipantsResponse {
    private Long gameId;
    private MatchRecordMode matchRecordMode;
    private List<FreeGameParticipantResponse> participants;
}
