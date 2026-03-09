package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class FreeGameDetailResponse {
    private Long gameId;
    private String title;
    private GameType gameType;
    private GameStatus gameStatus;
    private MatchRecordMode matchRecordMode;
    private GradeType gradeType;
    private Integer courtCount;
    private Integer roundCount;
    private Long organizerId;
    private String shareCode;

    public static FreeGameDetailResponse from(FreeGame freeGame, FreeGameSetting setting) {
        return FreeGameDetailResponse.builder()
                .gameId(freeGame.getId())
                .title(freeGame.getTitle())
                .gameType(freeGame.getGameType())
                .gameStatus(freeGame.getGameStatus())
                .matchRecordMode(freeGame.getMatchRecordMode())
                .gradeType(freeGame.getGradeType())
                .courtCount(setting.getCourtCount())
                .roundCount(setting.getRoundCount())
                .organizerId(freeGame.getOrganizer().getId())
                .shareCode(null)
                .build();
    }
}


