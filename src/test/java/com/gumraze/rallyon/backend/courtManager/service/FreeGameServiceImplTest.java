package com.gumraze.rallyon.backend.courtManager.service;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameRoundMatchResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.repository.*;
import com.gumraze.rallyon.backend.courtManager.service.support.FreeGameFixtures;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FreeGameServiceImplTest {
    @Mock
    GameRepository gameRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    GameParticipantRepository gameParticipantRepository;

    @Mock
    FreeGameSettingRepository freeGameSettingRepository;

    @Mock
    FreeGameRoundRepository freeGameRoundRepository;

    @Mock
    FreeGameMatchRepository freeGameMatchRepository;

    @Mock
    ShareCodeGenerator shareCodeGenerator;

    @InjectMocks
    FreeGameServiceImpl freeGameService;

    @Test
    @DisplayName("자유게임 라운드/매치 조회 성공 테스트")
    void getFreeGameRoundMatchResponse_success() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();

        User organizer = mock(User.class);
        when(organizer.getId()).thenReturn(userId);

        FreeGame freeGame = FreeGameFixtures.freeGame(gameId, organizer);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));

        FreeGameRound round1 = mock(FreeGameRound.class);
        when(round1.getId()).thenReturn(roundId);
        when(round1.getRoundNumber()).thenReturn(1);
        when(round1.getRoundStatus()).thenReturn(RoundStatus.NOT_STARTED);

        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(round1));

        GameParticipant a1 = mock(GameParticipant.class);
        GameParticipant a2 = mock(GameParticipant.class);
        GameParticipant b1 = mock(GameParticipant.class);
        GameParticipant b2 = mock(GameParticipant.class);
        when(a1.getId()).thenReturn(UUID.randomUUID());
        when(a2.getId()).thenReturn(UUID.randomUUID());
        when(b1.getId()).thenReturn(UUID.randomUUID());
        when(b2.getId()).thenReturn(UUID.randomUUID());

        FreeGameMatch m1 = mock(FreeGameMatch.class);
        when(m1.getRound()).thenReturn(round1);
        when(m1.getCourtNumber()).thenReturn(1);
        when(m1.getTeamAPlayer1()).thenReturn(a1);
        when(m1.getTeamAPlayer2()).thenReturn(a2);
        when(m1.getTeamBPlayer1()).thenReturn(b1);
        when(m1.getTeamBPlayer2()).thenReturn(b2);
        when(m1.getMatchStatus()).thenReturn(MatchStatus.NOT_STARTED);
        when(m1.getMatchResult()).thenReturn(null);
        when(m1.getIsActive()).thenReturn(true);
        when(freeGameMatchRepository.findByRoundIdInOrderByCourtNumber(List.of(roundId)))
                .thenReturn(List.of(m1));

        // when
        FreeGameRoundMatchResponse response =
                freeGameService.getFreeGameRoundMatchResponse(userId, gameId);

        // then
        assertThat(response.getGameId()).isEqualTo(gameId);
        assertThat(response.getRounds()).hasSize(1);
        assertThat(response.getRounds().get(0).getMatches()).hasSize(1);
        assertThat(response.getRounds().get(0).getMatches().get(0).getMatchResult())
                .isEqualTo(MatchResult.NULL);

        verify(gameRepository).findById(gameId);
        verify(freeGameRoundRepository).findByFreeGameIdOrderByRoundNumber(gameId);
        verify(freeGameMatchRepository).findByRoundIdInOrderByCourtNumber(List.of(roundId));
    }

    @Test
    @DisplayName("자유게임 라운드/매치 조회 시 organizer가 아니면 403 테스트")
    void getFreeGameRoundMatchResponse_withNotOrganizer_throwsForbidden() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();

        User organizer = mock(User.class);
        when(organizer.getId()).thenReturn(organizerId);

        FreeGame freeGame = FreeGameFixtures.freeGame(gameId, organizer);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));

        // when & then
        assertThatThrownBy(() -> freeGameService.getFreeGameRoundMatchResponse(userId, gameId))
                .isInstanceOf(ForbiddenException.class);
        verify(gameRepository).findById(gameId);
        verify(freeGameRoundRepository, never()).findByFreeGameIdOrderByRoundNumber(any(UUID.class));
        verify(freeGameMatchRepository, never()).findByRoundIdInOrderByCourtNumber(anyList());
    }
}
