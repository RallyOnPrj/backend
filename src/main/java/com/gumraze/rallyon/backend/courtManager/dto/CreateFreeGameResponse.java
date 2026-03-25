package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import java.util.UUID;

public record CreateFreeGameResponse(UUID gameId) {
    public static CreateFreeGameResponse from(FreeGame freeGame) {
        return new CreateFreeGameResponse(freeGame.getId());
    }
}
