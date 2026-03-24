package com.gumraze.rallyon.backend.courtManager.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameSettingRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameRepository;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import com.gumraze.rallyon.backend.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetPublicFreeGameDetailUseCaseTest {

    @InjectMocks
    FreeGameServiceImpl freeGameService;

    @Mock
    GameRepository gameRepository;
    @Mock
    FreeGameSettingRepository freeGameSettingRepository;


    @Test
    @DisplayName("shareCode로 공개 게임 상세 조회 성공")
    void getPublicFreeGameDetail_when_shareCode_exists_then_success() {
        // given: 생성된 게임이 존재하는 경우
        String shareCode = "public-share-code";
        UUID gameId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();

        FreeGame freeGame = FreeGame.builder()
                .id(gameId)
                .title("공개 게임")
                .organizer(User.builder().id(organizerId).build())
                .gradeType(GradeType.NATIONAL)
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .shareCode(shareCode)
                .build();

        FreeGameSetting setting = FreeGameSetting.builder()
                .id(UUID.randomUUID())
                .freeGame(freeGame)
                .courtCount(2)
                .roundCount(3)
                .build();

        when(gameRepository.findByShareCode(shareCode)).thenReturn(Optional.of(freeGame));
        when(freeGameSettingRepository.findByFreeGameId(gameId)).thenReturn(Optional.of(setting));

        // when: 공유 서비스를 호출 했을 때
        FreeGameDetailResponse response =
                freeGameService.getPublicFreeGameDetail(shareCode);

        // then: 게임이 반환되어야함.
        assertThat(response).isNotNull();
        assertThat(response.getGameId()).isEqualTo(gameId);
        assertThat(response.getShareCode()).isEqualTo(shareCode);
        assertThat(response.getTitle()).isEqualTo("공개 게임");
    }

    @Test
    @DisplayName("shareCode가 없으면 공개 게임 상세 조회 실패")
    void getPublicFreeGameDetail_when_shareCode_not_found_then_throw_not_found() {
        // given
        String shareCode = "non-existing-share-code";

        when(gameRepository.findByShareCode(shareCode)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> freeGameService.getPublicFreeGameDetail(shareCode))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("게임 설정이 없으면 공개 게임 상세 조회 실패")
    void getPublicFreeGameDetail_when_setting_not_found_then_throw_not_found() {
        // given
        String shareCode = "public-share-code";
        UUID gameId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();

        FreeGame freeGame = FreeGame.builder()
                .id(gameId)
                .title("공개 게임")
                .organizer(User.builder().id(organizerId).build())
                .gradeType(GradeType.NATIONAL)
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .shareCode(shareCode)
                .build();

        when(gameRepository.findByShareCode(shareCode)).thenReturn(Optional.of(freeGame));
        when(freeGameSettingRepository.findByFreeGameId(gameId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> freeGameService.getPublicFreeGameDetail(shareCode))
                .isInstanceOf(NotFoundException.class);
    }

}
