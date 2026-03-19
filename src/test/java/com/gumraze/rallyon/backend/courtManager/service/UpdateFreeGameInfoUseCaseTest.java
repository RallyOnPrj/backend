package com.gumraze.rallyon.backend.courtManager.service;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRequest;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.repository.*;
import com.gumraze.rallyon.backend.courtManager.service.support.FreeGameFixtures;
import com.gumraze.rallyon.backend.courtManager.service.support.FreeGameServiceTestSupport;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFreeGameInfoUseCaseTest implements FreeGameServiceTestSupport {

    @Mock GameRepository gameRepository;
    @Mock UserRepository userRepository;
    @Mock GameParticipantRepository gameParticipantRepository;
    @Mock FreeGameSettingRepository freeGameSettingRepository;
    @Mock FreeGameRoundRepository freeGameRoundRepository;
    @Mock FreeGameMatchRepository freeGameMatchRepository;
    @Mock ShareCodeGenerator shareCodeGenerator;

    @InjectMocks
    FreeGameServiceImpl freeGameService;

    @Override
    public GameRepository gameRepository() {
        return gameRepository;
    }

    @Override
    public UserRepository userRepository() {
        return userRepository;
    }

    @Override
    public ShareCodeGenerator shareCodeGenerator() {
        return shareCodeGenerator;
    }

    @Test
    @DisplayName("자유게임 기본 정보 수정 성공 시 title, gradeType, matchRecordMode를 반영한다")
    void updateFreeGameInfo_success() {
        // given: 수정할 자유게임과 기본 정보 수정 요청을 준비한다.
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        User organizer = organizer(userId);

        UpdateFreeGameRequest request = UpdateFreeGameRequest.builder()
                .title("수정된 게임 제목")
                .matchRecordMode(MatchRecordMode.RESULT)
                .gradeType(GradeType.REGIONAL)
                .build();

        FreeGame freeGame = FreeGameFixtures.freeGame(gameId, organizer);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));
        when(gameRepository.save(any(FreeGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when: 자유게임 기본 정보 수정을 수행한다.
        UpdateFreeGameResponse response = freeGameService.updateFreeGameInfo(userId, gameId, request);

        // then: 저장되는 엔티티에 변경된 기본 정보가 반영되어야 한다.
        ArgumentCaptor<FreeGame> captor = ArgumentCaptor.forClass(FreeGame.class);
        verify(gameRepository).save(captor.capture());

        FreeGame savedFreeGame = captor.getValue();
        assertThat(response.getGameId()).isEqualTo(gameId);
        assertThat(savedFreeGame.getId()).isEqualTo(gameId);
        assertThat(savedFreeGame.getTitle()).isEqualTo(request.getTitle());
        assertThat(savedFreeGame.getGradeType()).isEqualTo(request.getGradeType());
        assertThat(savedFreeGame.getMatchRecordMode()).isEqualTo(request.getMatchRecordMode());
    }

    @Test
    @DisplayName("자유게임 기본 정보 수정 시 location을 변경한다")
    void updateFreeGameInfo_updatesLocation() {
        // given: 기존 location이 있는 자유게임과 새로운 location 수정 요청을 준비한다.
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        User organizer = organizer(userId);

        FreeGame freeGame = FreeGameFixtures.freeGame(gameId, organizer, "기존 장소");

        UpdateFreeGameRequest request = UpdateFreeGameRequest.builder()
                .title("수정된 게임")
                .location("올림픽공원 배드민턴장")
                .gradeType(GradeType.REGIONAL)
                .matchRecordMode(MatchRecordMode.RESULT)
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));
        when(gameRepository.save(any(FreeGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when: 자유게임 기본 정보 수정을 수행한다.
        freeGameService.updateFreeGameInfo(userId, gameId, request);

        // then: 저장되는 엔티티의 location이 변경되어야 한다.
        ArgumentCaptor<FreeGame> captor = ArgumentCaptor.forClass(FreeGame.class);
        verify(gameRepository).save(captor.capture());

        FreeGame savedFreeGame = captor.getValue();
        assertThat(savedFreeGame.getLocation()).isEqualTo("올림픽공원 배드민턴장");
    }

    @Test
    @DisplayName("자유게임 기본 정보 수정 시 요청자가 생성자가 아니면 예외가 발생한다")
    void updateFreeGameInfo_withoutPermission_throwsForbidden() {
        // given: 요청자와 organizer가 다른 자유게임과 수정 요청을 준비한다.
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UpdateFreeGameRequest request = UpdateFreeGameRequest.builder()
                .title("수정된 게임 제목")
                .matchRecordMode(MatchRecordMode.RESULT)
                .gradeType(GradeType.REGIONAL)
                .build();

        User organizer = organizer(UUID.randomUUID());

        FreeGame freeGame = FreeGameFixtures.freeGame(gameId, organizer);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));

        // when & then: organizer가 아니면 예외가 발생해야 한다.
        assertThatThrownBy(() -> freeGameService.updateFreeGameInfo(userId, gameId, request))
                .isInstanceOf(ForbiddenException.class);
    }
}
