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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

public final class CourtManagerControllerFixtures {

    private CourtManagerControllerFixtures() {
    }

    public static RequestPostProcessor authenticatedUser(Long userId) {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    public static FreeGameDetailResponse freeGameDetailResponse(Long organizerId, Long gameId) {
        return FreeGameDetailResponse.builder()
                .gameId(gameId)
                .title("자유게임")
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .gradeType(GradeType.NATIONAL)
                .location("잠실 배드민턴장")
                .courtCount(2)
                .roundCount(2)
                .organizerId(organizerId)
                .build();
    }

    public static FreeGameParticipantResponse participantResponse(Long participantId, Long userId, String displayName) {
        return FreeGameParticipantResponse.builder()
                .participantId(participantId)
                .userId(userId)
                .displayName(displayName)
                .gender(Gender.MALE)
                .grade(Grade.ROOKIE)
                .ageGroup(30)
                .build();
    }

    public static FreeGameParticipantResponse participantResponseWithStats(
            FreeGameParticipantResponse participant,
            int assignedMatchCount,
            int completedMatchCount,
            int winCount,
            int lossCount
    ) {
        return FreeGameParticipantResponse.builder()
                .participantId(participant.getParticipantId())
                .userId(participant.getUserId())
                .displayName(participant.getDisplayName())
                .gender(participant.getGender())
                .grade(participant.getGrade())
                .ageGroup(participant.getAgeGroup())
                .assignedMatchCount(assignedMatchCount)
                .completedMatchCount(completedMatchCount)
                .winCount(winCount)
                .lossCount(lossCount)
                .build();
    }

    public static FreeGameParticipantsResponse participantsResponse(
            Long gameId,
            List<FreeGameParticipantResponse> participants
    ) {
        return FreeGameParticipantsResponse.builder()
                .gameId(gameId)
                .matchRecordMode(MatchRecordMode.RESULT)
                .participants(participants)
                .build();
    }

    public static FreeGameParticipantDetailResponse participantDetailResponse(
            Long gameId,
            Long participantId,
            Long userId,
            String displayName
    ) {
        return FreeGameParticipantDetailResponse.builder()
                .gameId(gameId)
                .participantId(participantId)
                .userId(userId)
                .displayName(displayName)
                .gender(Gender.MALE)
                .grade(Grade.ROOKIE)
                .ageGroup(30)
                .build();
    }
}
