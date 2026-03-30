package com.gumraze.rallyon.backend.courtManager.dto;

import java.util.List;
import java.util.UUID;

public record FreeGameRoundMatchResponse(
        UUID gameId,
        List<FreeGameRoundResponse> rounds
) {
    public static FreeGameRoundMatchResponse from(UUID gameId, List<FreeGameRoundResponse> rounds) {
        return new FreeGameRoundMatchResponse(gameId, rounds);
    }
}
