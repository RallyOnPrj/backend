package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CreateFreeGameResponse {
    private UUID gameId;

    public static CreateFreeGameResponse from(FreeGame freeGame) {
        return CreateFreeGameResponse.builder()
                .gameId(freeGame.getId())
                .build();
    }
}
