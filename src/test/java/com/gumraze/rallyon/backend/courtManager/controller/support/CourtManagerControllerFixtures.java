package com.gumraze.rallyon.backend.courtManager.controller.support;

import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantsResponse;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

public final class CourtManagerControllerFixtures {

    private CourtManagerControllerFixtures() {
    }

    public static RequestPostProcessor authenticatedUser(UUID userId) {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    public static FreeGameDetailResponse freeGameDetailResponse(UUID organizerId, UUID gameId) {
        return new FreeGameDetailResponse(
                gameId,
                "자유게임",
                GameType.FREE,
                GameStatus.NOT_STARTED,
                MatchRecordMode.STATUS_ONLY,
                GradeType.NATIONAL,
                2,
                2,
                organizerId,
                null,
                "잠실 배드민턴장"
        );
    }

    public static FreeGameParticipantResponse participantResponse(UUID participantId, UUID userId, String displayName) {
        return new FreeGameParticipantResponse(
                participantId,
                userId,
                displayName,
                Gender.MALE,
                Grade.ROOKIE,
                30,
                null,
                null,
                null,
                null
        );
    }

    public static FreeGameParticipantResponse participantResponseWithStats(
            FreeGameParticipantResponse participant,
            int assignedMatchCount,
            int completedMatchCount,
            int winCount,
            int lossCount
    ) {
        return new FreeGameParticipantResponse(
                participant.participantId(),
                participant.identityAccountId(),
                participant.displayName(),
                participant.gender(),
                participant.grade(),
                participant.ageGroup(),
                assignedMatchCount,
                completedMatchCount,
                winCount,
                lossCount
        );
    }

    public static FreeGameParticipantsResponse participantsResponse(
            UUID gameId,
            List<FreeGameParticipantResponse> participants
    ) {
        return new FreeGameParticipantsResponse(gameId, MatchRecordMode.RESULT, participants);
    }

    public static FreeGameParticipantDetailResponse participantDetailResponse(
            UUID gameId,
            UUID participantId,
            UUID userId,
            String displayName
    ) {
        return new FreeGameParticipantDetailResponse(
                gameId,
                participantId,
                userId,
                displayName,
                Gender.MALE,
                Grade.ROOKIE,
                30
        );
    }
}
