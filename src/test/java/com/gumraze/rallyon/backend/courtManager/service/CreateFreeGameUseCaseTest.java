package com.gumraze.rallyon.backend.courtManager.service;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.repository.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateFreeGameUseCaseTest {

    @Mock private GameRepository gameRepository;
    @Mock private GameParticipantRepository gameParticipantRepository;
    @Mock private FreeGameSettingRepository freeGameSettingRepository;
    @Mock private UserRepository userRepository;
    @Mock private FreeGameRoundRepository freeGameRoundRepository;
    @Mock private FreeGameMatchRepository freeGameMatchRepository;
    @Mock private ShareCodeGenerator shareCodeGenerator;

    @InjectMocks
    private FreeGameServiceImpl freeGameService;

    @Test
    @DisplayName("자유게임 생성 시 shareCode를 생성해서 저장한다.")
    void createFreeGame_when_request_valid_then_generate_share_code() {
        // given: 유효한 생성 요청과 organizer가 존재하고, 생성기가 새 shareCode를 반환한다.
        Long userId = 1L;
        String generatedShareCode = "generated-share-code";

        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("공유 게임")
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(3)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .build();

        User organizer = User.builder().id(userId).build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(organizer));
        when(shareCodeGenerator.generate()).thenReturn(generatedShareCode);
        when(gameRepository.existsByShareCode(generatedShareCode)).thenReturn(false);
        when(gameRepository.save(any(FreeGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when: 자유게임 생성을 호출한다.
        CreateFreeGameResponse response = freeGameService.createFreeGame(userId, request);

        // then: 저장되는 게임 엔티티에 shareCode가 채워지고, 생성 응답도 받는다.
        ArgumentCaptor<FreeGame> gameCaptor = ArgumentCaptor.forClass(FreeGame.class);
        verify(gameRepository).save(gameCaptor.capture());

        FreeGame savedGame = gameCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(savedGame.getShareCode()).isEqualTo(generatedShareCode);
    }

    @Test
    @DisplayName("생성한 shareCode가 이미 존재하면 새 코드를 다시 생성한다.")
    void createFreeGame_when_share_code_collides_then_regenerate() {
        // given: 첫 번째 shareCode는 이미 존재하고, 두 번째 shareCode는 사용 가능한 상태이다.
        Long userId = 1L;
        String firstGeneratedShareCode = "first-generated-share-code";
        String secondGeneratedShareCode = "second-generated-share-code";

        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("공유 게임")
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(3)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .build();

        User organizer = User.builder()
                .id(userId)
                .build();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(organizer));
        when(shareCodeGenerator.generate()).thenReturn(firstGeneratedShareCode, secondGeneratedShareCode);
        when(gameRepository.existsByShareCode(firstGeneratedShareCode)).thenReturn(true);
        when(gameRepository.existsByShareCode(secondGeneratedShareCode)).thenReturn(false);
        when(gameRepository.save(any(FreeGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when: 자유 게임 생성 호출
        freeGameService.createFreeGame(userId, request);

        // then: 중복된 첫 번째 코드는 버리고, 두 번째 shareCode가 저장된다.
        ArgumentCaptor<FreeGame> gameCaptor = ArgumentCaptor.forClass(FreeGame.class);
        verify(gameRepository).save(gameCaptor.capture());

        FreeGame savedGame = gameCaptor.getValue();
        assertThat(savedGame.getShareCode()).isEqualTo(secondGeneratedShareCode);
    }

    @Test
    @DisplayName("shareCode 생성이 최대 재시도 횟수를 초과하면 예외가 발생한다.")
    void createFreeGame_when_share_code_collision_exceeds_max_retries_then_throw() {
        // given: 생성된 모든 shareCode가 이미 존재한다.
        Long userId = 1L;
        String collidingShareCode = "always-colliding-share-code";

        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("공유 게임")
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(3)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .build();

        User organizer = User.builder()
                .id(userId)
                .build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(organizer));
        when(shareCodeGenerator.generate()).thenReturn(collidingShareCode);
        when(gameRepository.existsByShareCode(collidingShareCode)).thenReturn(true);

        // when & then: 최대 재시도 횟수를 초과하면 예외를 던진다.
        assertThatThrownBy(() -> freeGameService.createFreeGame(userId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shareCode");
    }
}
