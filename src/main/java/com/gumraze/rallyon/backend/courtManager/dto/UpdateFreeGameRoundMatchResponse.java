package com.gumraze.rallyon.backend.courtManager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UpdateFreeGameRoundMatchResponse {
    private final UUID gameId;

    public static UpdateFreeGameRoundMatchResponse from(UUID gameId) {
        return new UpdateFreeGameRoundMatchResponse(gameId);
    }
}
