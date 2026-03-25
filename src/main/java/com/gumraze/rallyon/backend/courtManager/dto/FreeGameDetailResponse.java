package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import com.gumraze.rallyon.backend.user.constants.GradeType;

import java.util.UUID;

public record FreeGameDetailResponse(
        UUID gameId,
        String title,
        GameType gameType,
        GameStatus gameStatus,
        MatchRecordMode matchRecordMode,
        GradeType gradeType,
        Integer courtCount,
        Integer roundCount,
        UUID organizerIdentityAccountId,
        String shareCode,
        String location
) {
    public static FreeGameDetailResponse from(FreeGame freeGame, FreeGameSetting setting) {
        return new FreeGameDetailResponse(
                freeGame.getId(),
                freeGame.getTitle(),
                freeGame.getGameType(),
                freeGame.getGameStatus(),
                freeGame.getMatchRecordMode(),
                freeGame.getGradeType(),
                setting.getCourtCount(),
                setting.getRoundCount(),
                freeGame.getOrganizerIdentityAccountId(),
                freeGame.getShareCode(),
                freeGame.getLocation()
        );
    }
}
