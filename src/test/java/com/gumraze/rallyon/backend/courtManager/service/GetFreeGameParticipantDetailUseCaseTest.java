package com.gumraze.rallyon.backend.courtManager.service;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantDetailResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@ExtendWith(MockitoExtension.class)
public class GetFreeGameParticipantDetailUseCaseTest {

    @InjectMocks
    private FreeGameServiceImpl freeGameService;

    @Mock private GameRepository gameRepository;
    @Mock private GameParticipantRepository gameParticipantRepository;
    @Mock private FreeGameSettingRepository freeGameSettingRepository;
    @Mock private UserRepository userRepository;
    @Mock private FreeGameRoundRepository freeGameRoundRepository;
    @Mock private FreeGameMatchRepository freeGameMatchRepository;

    @Test
    @DisplayName("게임이 없으면 상세 조회 실패")
    void getFreeGameParticipantDetail_when_game_not_exist_then_throw_exception() {
        // given
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                freeGameService.getFreeGameParticipantDetail(userId, gameId, participantId)
        ).isInstanceOf(NotFoundException.class);

        verifyNoInteractions(gameParticipantRepository);
    }

    @Test
    @DisplayName("organizer가 아니면 참가자 상태 조회 실패")
    void getFreeGameParticipantDetail_when_not_organizer_then_throw_forbidden() {
        // given
        UUID organizerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();  // organizer가 아님
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        stubFreeGame(gameId, organizerId, MatchRecordMode.STATUS_ONLY);

        // when & then
        assertThatThrownBy(() ->
                freeGameService.getFreeGameParticipantDetail(requesterId, gameId, participantId)
        ).isInstanceOf(ForbiddenException.class);

        verifyNoInteractions(gameParticipantRepository);
    }

    @Test
    @DisplayName("participant가 없으면 참가자 상세 조회 실패")
    void getFreeGameParticipantDetail_when_participant_not_found_then_throw_not_found() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        stubFreeGame(gameId, organizerId, MatchRecordMode.STATUS_ONLY);
        when(gameParticipantRepository.findById(participantId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                freeGameService.getFreeGameParticipantDetail(organizerId, gameId, participantId)
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("참가자가 다른 게임 소속이면 참가자 상세 조회 실패함.")
    void getFreeGameParticipantDetail_when_participant_not_belong_to_game_then_throw_not_found() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID otherGameId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        FreeGame targetGame = stubFreeGame(gameId, organizerId, MatchRecordMode.STATUS_ONLY);
        FreeGame otherGame = buildFreeGame(otherGameId, organizerId, MatchRecordMode.STATUS_ONLY);

        GameParticipant participant = GameParticipant.builder()
            .id(participantId)
            .freeGame(otherGame) // 핵심: 다른 게임 소속
            .user(null)
            .originalName("Kim")
            .displayName("Kim")
            .gender(Gender.MALE)
            .grade(Grade.ROOKIE)
            .ageGroup(30)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(targetGame));
        when(gameParticipantRepository.findById(participantId)).thenReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() ->
                freeGameService.getFreeGameParticipantDetail(organizerId, gameId, participantId)
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("회원 참가자 상세 조회 성공")
    void getFreeGameParticipantDetail_when_member_participant_then_success() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        UUID participantUserId = UUID.randomUUID();

        FreeGame freeGame = stubFreeGame(gameId, organizerId, MatchRecordMode.STATUS_ONLY);
        GameParticipant participant = buildParticipant(
                participantId,
                freeGame,
                "KimA",
                buildUser(participantUserId)
        );

        when(gameParticipantRepository.findById(participantId))
                .thenReturn(Optional.of(participant));

        // when
        FreeGameParticipantDetailResponse response =
                freeGameService.getFreeGameParticipantDetail(organizerId, gameId, participantId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGameId()).isEqualTo(gameId);
        assertThat(response.getParticipantId()).isEqualTo(participantId);
        assertThat(response.getUserId()).isEqualTo(participantUserId);
        assertThat(response.getDisplayName()).isEqualTo("KimA");
        assertThat(response.getGender()).isEqualTo(Gender.MALE);
        assertThat(response.getGrade()).isEqualTo(Grade.ROOKIE);
        assertThat(response.getAgeGroup()).isEqualTo(30);
    }

    @Test
    @DisplayName("비회원 참가자 상세 조회 성공")
    void getFreeGameParticipantDetail_when_guest_participant_then_success() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        FreeGame freeGame = stubFreeGame(gameId, organizerId, MatchRecordMode.STATUS_ONLY);
        GameParticipant participant = buildParticipant(participantId, freeGame, "GuestA", null);

        when(gameParticipantRepository.findById(participantId))
                .thenReturn(Optional.of(participant));

        // when
        FreeGameParticipantDetailResponse response =
                freeGameService.getFreeGameParticipantDetail(organizerId, gameId, participantId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGameId()).isEqualTo(gameId);
        assertThat(response.getParticipantId()).isEqualTo(participantId);
        assertThat(response.getUserId()).isNull();
        assertThat(response.getDisplayName()).isEqualTo("GuestA");
        assertThat(response.getGender()).isEqualTo(Gender.MALE);
        assertThat(response.getGrade()).isEqualTo(Grade.ROOKIE);
        assertThat(response.getAgeGroup()).isEqualTo(30);
    }

    // helper method
    private FreeGame stubFreeGame(UUID gameId, UUID organizerId, MatchRecordMode matchRecordMode) {
        FreeGame freeGame = buildFreeGame(gameId, organizerId, matchRecordMode);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));
        return freeGame;
    }

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

    private GameParticipant buildParticipant(UUID participantId, FreeGame freeGame, String displayName, User user) {
        return GameParticipant.builder()
                .id(participantId)
                .freeGame(freeGame)
                .user(user)
                .originalName(displayName)
                .displayName(displayName)
                .gender(Gender.MALE)
                .grade(Grade.ROOKIE)
                .ageGroup(30)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private User buildUser(UUID userId) {
        return User.builder()
                .id(userId)
                .build();
    }
}
