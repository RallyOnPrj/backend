package com.gumraze.rallyon.backend.courtManager.service;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import com.gumraze.rallyon.backend.courtManager.repository.*;
import com.gumraze.rallyon.backend.courtManager.service.support.FreeGameFixtures;
import com.gumraze.rallyon.backend.courtManager.service.support.FreeGameServiceTestSupport;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFreeGameDetailUseCaseTest implements FreeGameServiceTestSupport {

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
    @DisplayName("자유게임 상세 조회 성공 시 기본 정보와 설정을 매핑하여 반환한다")
    void getFreeGameDetail_success() {
        // given: 생성된 게임과 설정 정보가 존재한다.
        Long userId = 99L;
        Long gameId = 1L;
        User organizer = organizer(userId);

        FreeGame freeGame = FreeGameFixtures.freeGame(gameId, organizer);
        FreeGameSetting setting = FreeGameFixtures.setting(freeGame, 2, 3);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));
        when(freeGameSettingRepository.findByFreeGameId(gameId)).thenReturn(Optional.of(setting));

        // when: 자유게임 상세 조회를 수행한다.
        FreeGameDetailResponse response = freeGameService.getFreeGameDetail(userId, gameId);

        // then: 게임 기본 정보와 설정 정보가 응답에 포함되어야 한다.
        assertThat(response.getGameId()).isEqualTo(gameId);
        assertThat(response.getTitle()).isEqualTo(freeGame.getTitle());
        assertThat(response.getGameType()).isEqualTo(freeGame.getGameType());
        assertThat(response.getGameStatus()).isEqualTo(freeGame.getGameStatus());
        assertThat(response.getMatchRecordMode()).isEqualTo(freeGame.getMatchRecordMode());
        assertThat(response.getGradeType()).isEqualTo(freeGame.getGradeType());
        assertThat(response.getCourtCount()).isEqualTo(setting.getCourtCount());
        assertThat(response.getRoundCount()).isEqualTo(setting.getRoundCount());
        assertThat(response.getOrganizerId()).isEqualTo(freeGame.getOrganizer().getId());
    }

    @Test
    @DisplayName("자유게임 상세 조회 성공 시 location을 포함한다")
    void getFreeGameDetail_success_includesLocation() {
        // given: location이 저장된 자유게임과 설정 정보가 존재한다.
        Long userId = 99L;
        Long gameId = 1L;
        User organizer = organizer(userId);

        FreeGame freeGame = FreeGameFixtures.freeGame(gameId, organizer, "잠실 배드민턴장");
        FreeGameSetting setting = FreeGameFixtures.setting(freeGame, 2, 3);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));
        when(freeGameSettingRepository.findByFreeGameId(gameId)).thenReturn(Optional.of(setting));

        // when: 자유게임 상세 조회를 수행한다.
        FreeGameDetailResponse response = freeGameService.getFreeGameDetail(userId, gameId);

        // then: 응답에서 location을 읽을 수 있어야 한다.
        assertThat(response.getLocation()).isEqualTo("잠실 배드민턴장");
    }

    @Test
    @DisplayName("자유게임 상세 조회 시 존재하지 않는 gameId면 예외가 발생한다")
    void getFreeGameDetail_withUnknownGameId_throwsException() {
        // given: 존재하지 않는 gameId를 준비한다.
        Long userId = 1L;
        Long gameId = 99999L;

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        // when & then: 게임이 없으면 예외가 발생해야 한다.
        assertThatThrownBy(() -> freeGameService.getFreeGameDetail(userId, gameId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("자유게임 상세 조회 시 요청자가 생성자가 아니면 예외가 발생한다")
    void getFreeGameDetail_withNotOrganizer_throwsForbidden() {
        // given: 요청자와 organizer가 다른 자유게임이 존재한다.
        Long userId = 1L;
        Long gameId = 1L;

        User organizer = organizer(99L);
        FreeGame freeGame = FreeGameFixtures.freeGame(gameId, organizer);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));

        // when & then: organizer가 아니면 예외가 발생해야 한다.
        assertThatThrownBy(() -> freeGameService.getFreeGameDetail(userId, gameId))
                .isInstanceOf(ForbiddenException.class);
    }
}
