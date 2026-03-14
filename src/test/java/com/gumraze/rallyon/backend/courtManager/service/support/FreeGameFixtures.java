package com.gumraze.rallyon.backend.courtManager.service.support;

import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import com.gumraze.rallyon.backend.user.entity.User;

public final class FreeGameFixtures {

    private FreeGameFixtures() {
    }

    public static FreeGame freeGame(Long gameId, User organizer) {
        return FreeGame.builder()
                .id(gameId)
                .title("자유게임")
                .organizer(organizer)
                .gradeType(GradeType.NATIONAL)
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .shareCode(null)
                .build();
    }

    public static FreeGame freeGame(Long gameId, User organizer, String location) {
        return FreeGame.builder()
                .id(gameId)
                .title("자유게임")
                .location(location)
                .organizer(organizer)
                .gradeType(GradeType.NATIONAL)
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .shareCode(null)
                .build();
    }

    public static FreeGameSetting setting(FreeGame freeGame, int courtCount, int roundCount) {
        return FreeGameSetting.builder()
                .freeGame(freeGame)
                .courtCount(courtCount)
                .roundCount(roundCount)
                .build();
    }
}
