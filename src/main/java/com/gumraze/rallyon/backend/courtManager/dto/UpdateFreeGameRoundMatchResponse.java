package com.gumraze.rallyon.backend.courtManager.dto;

import java.util.UUID;

public record UpdateFreeGameRoundMatchResponse(UUID gameId) {
    public static UpdateFreeGameRoundMatchResponse from(UUID gameId) {
        return new UpdateFreeGameRoundMatchResponse(gameId);
    }
}
