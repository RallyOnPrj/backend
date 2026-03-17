package com.gumraze.rallyon.backend.courtManager.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class FreeGameRoundMatchResponse {
    UUID gameId;
    List<FreeGameRoundResponse> rounds;

    public static FreeGameRoundMatchResponse from(UUID gameId, List<FreeGameRoundResponse> rounds) {
        return FreeGameRoundMatchResponse.builder()
                .gameId(gameId)
                .rounds(rounds)
                .build();
    }
}
