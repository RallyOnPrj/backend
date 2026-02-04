package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.entity.FreeGame;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateFreeGameResponse {
    private Long gameId;

    public static CreateFreeGameResponse from(FreeGame freeGame) {
        return CreateFreeGameResponse.builder()
                .gameId(freeGame.getId())
                .build();
    }
}
