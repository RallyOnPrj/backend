package com.gumraze.rallyon.backend.courtManager.service;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.courtManager.constants.*;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantsResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.*;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetFreeGameParticipantsUseCaseTest {
    @InjectMocks
    private FreeGameServiceImpl freeGameService;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameParticipantRepository gameParticipantRepository;

    @Mock
    private FreeGameSettingRepository freeGameSettingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FreeGameRoundRepository freeGameRoundRepository;

    @Mock
    private FreeGameMatchRepository freeGameMatchRepository;

    @Test
    @DisplayName("Organizer가 아니면 참가자 목록을 조회할 수 없다")
    void get_participants_when_not_organizer_then_throw_forbidden() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        FreeGame freeGame = buildFreeGame(gameId, organizerId, MatchRecordMode.RESULT);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));

        // when & then
        assertThatThrownBy(() ->
                freeGameService.getFreeGameParticipants(requesterId, gameId, false)
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("includeStats=false면 stats 필드는 포함되지 않는다")
    void get_participants_without_stats() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        LocalDateTime earlier = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime later = earlier.plusMinutes(5);

        FreeGame freeGame = buildFreeGame(gameId, organizerId, MatchRecordMode.RESULT);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));

        GameParticipant p1 = buildParticipant(UUID.randomUUID(), freeGame, "KimB", null, earlier);
        GameParticipant p2 = buildParticipant(UUID.randomUUID(), freeGame, "KimA", buildUser(UUID.randomUUID()), later);
        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2));

        // when
        FreeGameParticipantsResponse response = freeGameService.getFreeGameParticipants(organizerId, gameId, false);

        // then
        assertThat(response.getParticipants()).hasSize(2);
        assertThat(response.getParticipants().get(0).getDisplayName()).isEqualTo("KimB");
        assertThat(response.getParticipants().get(1).getDisplayName()).isEqualTo("KimA");
        Map<String, FreeGameParticipantResponse> participantsByName = response.getParticipants().stream()
                .collect(Collectors.toMap(FreeGameParticipantResponse::getDisplayName, Function.identity()));

        assertThat(participantsByName.get("KimA").getParticipantId()).isEqualTo(p2.getId());
        assertThat(participantsByName.get("KimB").getParticipantId()).isEqualTo(p1.getId());

        FreeGameParticipantResponse first = participantsByName.get("KimA");
        assertThat(first.getAssignedMatchCount()).isNull();
        assertThat(first.getCompletedMatchCount()).isNull();
        assertThat(first.getWinCount()).isNull();
        assertThat(first.getLossCount()).isNull();

        verifyNoInteractions(freeGameRoundRepository, freeGameMatchRepository);
    }

    @Test
    @DisplayName("includeStats=true & RESULT면 승패/완료/배정 집계가 포함된다")
    void get_participants_with_stats_when_result_mode() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();

        FreeGame freeGame = buildFreeGame(gameId, organizerId, MatchRecordMode.RESULT);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));

        GameParticipant p1 = buildParticipant(UUID.randomUUID(), freeGame, "A", null);
        GameParticipant p2 = buildParticipant(UUID.randomUUID(), freeGame, "B", null);
        GameParticipant p3 = buildParticipant(UUID.randomUUID(), freeGame, "C", null);
        GameParticipant p4 = buildParticipant(UUID.randomUUID(), freeGame, "D", null);
        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2, p3, p4));

        UUID roundId = UUID.randomUUID();
        FreeGameRound round = buildRound(roundId, freeGame, 1);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(round));

        FreeGameMatch match = buildMatch(
                round,
                p1,
                p2,
                p3,
                p4,
                MatchStatus.COMPLETED,
                MatchResult.TEAM_A_WIN
        );
        when(freeGameMatchRepository.findByRoundIdInOrderByCourtNumber(List.of(roundId)))
                .thenReturn(List.of(match));

        // when
        FreeGameParticipantsResponse response = freeGameService.getFreeGameParticipants(organizerId, gameId, true);

        // then
        List<FreeGameParticipantResponse> participants = response.getParticipants();
        assertThat(participants).hasSize(4);

        Map<String, FreeGameParticipantResponse> participantsByName = participants.stream()
                .collect(Collectors.toMap(FreeGameParticipantResponse::getDisplayName, Function.identity()));

        FreeGameParticipantResponse teamA1 = participantsByName.get("A");
        assertThat(teamA1.getAssignedMatchCount()).isEqualTo(1);
        assertThat(teamA1.getCompletedMatchCount()).isEqualTo(1);
        assertThat(teamA1.getWinCount()).isEqualTo(1);
        assertThat(teamA1.getLossCount()).isEqualTo(0);

        FreeGameParticipantResponse teamB1 = participantsByName.get("C");
        assertThat(teamB1.getAssignedMatchCount()).isEqualTo(1);
        assertThat(teamB1.getCompletedMatchCount()).isEqualTo(1);
        assertThat(teamB1.getWinCount()).isEqualTo(0);
        assertThat(teamB1.getLossCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("includeStats=true & STATUS_ONLY면 승패는 제외하고 집계한다")
    void get_participants_with_stats_when_status_only_mode() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();

        FreeGame freeGame = buildFreeGame(gameId, organizerId, MatchRecordMode.STATUS_ONLY);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));

        GameParticipant p1 = buildParticipant(UUID.randomUUID(), freeGame, "A", null);
        GameParticipant p2 = buildParticipant(UUID.randomUUID(), freeGame, "B", null);
        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2));

        UUID roundId = UUID.randomUUID();
        FreeGameRound round = buildRound(roundId, freeGame, 1);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(round));

        FreeGameMatch match = buildMatch(
                round,
                p1,
                null,
                p2,
                null,
                MatchStatus.COMPLETED,
                MatchResult.TEAM_A_WIN
        );
        when(freeGameMatchRepository.findByRoundIdInOrderByCourtNumber(List.of(roundId)))
                .thenReturn(List.of(match));

        // when
        FreeGameParticipantsResponse response = freeGameService.getFreeGameParticipants(organizerId, gameId, true);

        // then
        FreeGameParticipantResponse first = response.getParticipants().get(0);
        assertThat(first.getAssignedMatchCount()).isEqualTo(1);
        assertThat(first.getCompletedMatchCount()).isEqualTo(1);
        assertThat(first.getWinCount()).isNull();
        assertThat(first.getLossCount()).isNull();
    }

    // Helper 메서드

    private FreeGame buildFreeGame(UUID gameId, UUID organizerId, MatchRecordMode matchRecordMode) {
        return FreeGame.builder()
                .id(gameId)
                .title("테스트 게임")
                .organizer(buildUser(organizerId))
                .gradeType(GradeType.NATIONAL)
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(matchRecordMode)
                .build();
    }

    private GameParticipant buildParticipant(UUID id, FreeGame freeGame, String displayName, User user) {
        return buildParticipant(id, freeGame, displayName, user, LocalDateTime.now());
    }

    private GameParticipant buildParticipant(
            UUID id,
            FreeGame freeGame,
            String displayName,
            User user,
            LocalDateTime createdAt
    ) {
        return GameParticipant.builder()
                .id(id)
                .freeGame(freeGame)
                .user(user)
                .originalName(displayName)
                .displayName(displayName)
                .gender(Gender.MALE)
                .grade(Grade.ROOKIE)
                .ageGroup(30)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    private User buildUser(UUID id) {
        return User.builder()
                .id(id)
                .build();
    }

    private FreeGameRound buildRound(UUID roundId, FreeGame freeGame, int roundNumber) {
        return FreeGameRound.builder()
                .id(roundId)
                .freeGame(freeGame)
                .roundNumber(roundNumber)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private FreeGameMatch buildMatch(
            FreeGameRound round,
            GameParticipant teamA1,
            GameParticipant teamA2,
            GameParticipant teamB1,
            GameParticipant teamB2,
            MatchStatus status,
            MatchResult result
    ) {
        return FreeGameMatch.builder()
                .round(round)
                .courtNumber(1)
                .teamAPlayer1(teamA1)
                .teamAPlayer2(teamA2)
                .teamBPlayer1(teamB1)
                .teamBPlayer2(teamB2)
                .matchStatus(status)
                .matchResult(result)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
