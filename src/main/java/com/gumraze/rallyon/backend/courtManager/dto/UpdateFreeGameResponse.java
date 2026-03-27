package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import java.util.UUID;

public record UpdateFreeGameResponse(UUID gameId) {
    public static UpdateFreeGameResponse from(FreeGame freeGame) {
        return new UpdateFreeGameResponse(freeGame.getId());
    }
}
