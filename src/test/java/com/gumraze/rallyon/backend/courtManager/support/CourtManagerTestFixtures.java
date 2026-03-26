package com.gumraze.rallyon.backend.courtManager.support;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

public final class CourtManagerTestFixtures {

    private CourtManagerTestFixtures() {
    }

    public static FreeGame freeGame(UUID gameId, UUID organizerAccountId, MatchRecordMode matchRecordMode) {
        FreeGame freeGame = FreeGame.create(
                "자유게임",
                organizerAccountId,
                GradeType.NATIONAL,
                matchRecordMode,
                "share-code",
                "잠실 배드민턴장"
        );
        ReflectionTestUtils.setField(freeGame, "id", gameId);
        return freeGame;
    }

    public static FreeGameSetting setting(FreeGame freeGame, int courtCount, int roundCount) {
        return FreeGameSetting.create(freeGame, courtCount, roundCount);
    }

    public static GameParticipant participant(
            FreeGame freeGame,
            UUID participantId,
            UUID accountId,
            String originalName,
            String displayName,
            Gender gender,
            Grade grade,
            Integer ageGroup,
            LocalDateTime createdAt
    ) {
        GameParticipant participant = GameParticipant.create(
                freeGame,
                accountId,
                originalName,
                displayName,
                gender,
                grade,
                ageGroup
        );
        ReflectionTestUtils.setField(participant, "id", participantId);
        ReflectionTestUtils.setField(participant, "createdAt", createdAt);
        ReflectionTestUtils.setField(participant, "updatedAt", createdAt);
        return participant;
    }

    public static FreeGameRound round(FreeGame freeGame, UUID roundId, int roundNumber, RoundStatus roundStatus) {
        FreeGameRound round = FreeGameRound.create(freeGame, roundNumber, roundStatus);
        ReflectionTestUtils.setField(round, "id", roundId);
        return round;
    }

    public static FreeGameMatch match(
            FreeGameRound round,
            UUID matchId,
            int courtNumber,
            GameParticipant teamAPlayer1,
            GameParticipant teamAPlayer2,
            GameParticipant teamBPlayer1,
            GameParticipant teamBPlayer2,
            MatchStatus matchStatus,
            MatchResult matchResult,
            boolean isActive
    ) {
        FreeGameMatch match = FreeGameMatch.create(
                round,
                courtNumber,
                teamAPlayer1,
                teamAPlayer2,
                teamBPlayer1,
                teamBPlayer2,
                null,
                matchStatus,
                matchResult,
                isActive
        );
        ReflectionTestUtils.setField(match, "id", matchId);
        return match;
    }
}
