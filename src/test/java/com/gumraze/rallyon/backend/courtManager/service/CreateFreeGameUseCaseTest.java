package com.gumraze.rallyon.backend.courtManager.service;

import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.dto.ParticipantCreateRequest;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.repository.*;
import com.gumraze.rallyon.backend.courtManager.service.support.FreeGameServiceTestSupport;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateFreeGameUseCaseTest implements FreeGameServiceTestSupport {

    @Mock private GameRepository gameRepository;
    @Mock private GameParticipantRepository gameParticipantRepository;
    @Mock private FreeGameSettingRepository freeGameSettingRepository;
    @Mock private UserRepository userRepository;
    @Mock private FreeGameRoundRepository freeGameRoundRepository;
    @Mock private FreeGameMatchRepository freeGameMatchRepository;
    @Mock private ShareCodeGenerator shareCodeGenerator;

    @InjectMocks
    private FreeGameServiceImpl freeGameService;

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
    @DisplayName("자유게임 생성 성공 시 gameId를 반환한다")
    void createFreeGame_success_returnsGameId() {
        // given: 최소 필수값이 포함된 자유게임 생성 요청과 organizer가 존재한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .gradeType(GradeType.NATIONAL)
                .courtCount(1)
                .roundCount(1)
                .build();

        User organizer = organizer(1L);
        stubOrganizer(1L, organizer);
        stubShareCode("share-code-123");

        FreeGame savedFreeGame = FreeGame.builder()
                .id(1L)
                .title(request.getTitle())
                .organizer(organizer)
                .gradeType(GradeType.NATIONAL)
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .shareCode("share-code-123")
                .build();

        when(gameRepository.save(any(FreeGame.class))).thenReturn(savedFreeGame);

        // when: 자유게임 생성을 수행한다.
        CreateFreeGameResponse createdGame = freeGameService.createFreeGame(1L, request);

        // then: 생성 응답에 저장된 gameId가 반환되어야 한다.
        assertThat(createdGame).isNotNull();
        assertThat(createdGame.getGameId()).isEqualTo(savedFreeGame.getId());
        verify(gameRepository).save(any(FreeGame.class));
    }

    @Test
    @DisplayName("자유게임 생성 시 shareCode를 생성해서 저장한다")
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
        stubOrganizer(userId, organizer);
        when(shareCodeGenerator.generate()).thenReturn(generatedShareCode);
        when(gameRepository.existsByShareCode(generatedShareCode)).thenReturn(false);
        when(gameRepository.save(any(FreeGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when: 자유게임 생성을 수행한다.
        CreateFreeGameResponse response = freeGameService.createFreeGame(userId, request);

        // then: 저장되는 게임 엔티티에 shareCode가 채워지고, 생성 응답도 받아야 한다.
        ArgumentCaptor<FreeGame> gameCaptor = ArgumentCaptor.forClass(FreeGame.class);
        verify(gameRepository).save(gameCaptor.capture());

        FreeGame savedGame = gameCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(savedGame.getShareCode()).isEqualTo(generatedShareCode);
    }

    @Test
    @DisplayName("생성한 shareCode가 이미 존재하면 새 코드를 다시 생성한다")
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
        stubOrganizer(userId, organizer);
        when(shareCodeGenerator.generate()).thenReturn(firstGeneratedShareCode, secondGeneratedShareCode);
        when(gameRepository.existsByShareCode(firstGeneratedShareCode)).thenReturn(true);
        when(gameRepository.existsByShareCode(secondGeneratedShareCode)).thenReturn(false);
        when(gameRepository.save(any(FreeGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when: 자유게임 생성을 수행한다.
        freeGameService.createFreeGame(userId, request);

        // then: 중복된 첫 번째 코드는 버리고, 두 번째 shareCode가 저장되어야 한다.
        ArgumentCaptor<FreeGame> gameCaptor = ArgumentCaptor.forClass(FreeGame.class);
        verify(gameRepository).save(gameCaptor.capture());

        FreeGame savedGame = gameCaptor.getValue();
        assertThat(savedGame.getShareCode()).isEqualTo(secondGeneratedShareCode);
    }

    @Test
    @DisplayName("shareCode 생성이 최대 재시도 횟수를 초과하면 예외가 발생한다")
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

        stubOrganizer(userId, organizer);
        when(shareCodeGenerator.generate()).thenReturn(collidingShareCode);
        when(gameRepository.existsByShareCode(collidingShareCode)).thenReturn(true);

        // when & then: 최대 재시도 횟수를 초과하면 예외를 던져야 한다.
        assertThatThrownBy(() -> freeGameService.createFreeGame(userId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shareCode");
    }

    @Test
    @DisplayName("자유게임 생성 시 matchRecordMode가 null이면 STATUS_ONLY를 사용한다")
    void createFreeGame_withNoMatchRecordMode_returnsStatusOnly() {
        // given: title, gradeType, courtCount, roundCount만 입력되고 matchRecordMode는 입력되지 않는다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임1")
                .gradeType(GradeType.NATIONAL)
                .courtCount(1)
                .roundCount(1)
                .build();

        when(gameRepository.save(any(FreeGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User organizer = organizer(1L);
        stubOrganizer(1L, organizer);
        stubShareCode("share-code-123");

        // when: 자유게임 생성을 수행한다.
        freeGameService.createFreeGame(1L, request);

        // then: 저장되는 FreeGame의 matchRecordMode는 STATUS_ONLY여야 한다.
        ArgumentCaptor<FreeGame> captor = ArgumentCaptor.forClass(FreeGame.class);
        verify(gameRepository).save(captor.capture());

        FreeGame savedFreeGame = captor.getValue();
        assertThat(savedFreeGame.getMatchRecordMode()).isEqualTo(MatchRecordMode.STATUS_ONLY);
    }

    @Test
    @DisplayName("자유게임 생성 시 matchRecordMode가 RESULT면 그대로 저장된다")
    void createFreeGame_withResultMatchRecordMode_returnsResult() {
        // given: matchRecordMode가 RESULT인 자유게임 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임1")
                .gradeType(GradeType.NATIONAL)
                .courtCount(1)
                .roundCount(1)
                .matchRecordMode(MatchRecordMode.RESULT)
                .build();

        when(gameRepository.save(any(FreeGame.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubOrganizer(1L, organizer(1L));
        stubShareCode("share-code-123");

        // when: 자유게임 생성을 수행한다.
        freeGameService.createFreeGame(1L, request);

        // then: 저장되는 FreeGame의 matchRecordMode는 RESULT여야 한다.
        ArgumentCaptor<FreeGame> captor = ArgumentCaptor.forClass(FreeGame.class);
        verify(gameRepository).save(captor.capture());

        FreeGame savedFreeGame = captor.getValue();
        assertThat(savedFreeGame.getMatchRecordMode()).isEqualTo(MatchRecordMode.RESULT);
    }

    @Test
    @DisplayName("자유게임 생성 시 gameType과 gameStatus는 기본값으로 저장된다")
    void createFreeGame_withDefaultGameTypeAndStatus_returnsDefault() {
        // given: 필수 입력값만 있는 자유게임 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임1")
                .gradeType(GradeType.NATIONAL)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .courtCount(1)
                .roundCount(1)
                .build();

        when(gameRepository.save(any(FreeGame.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubOrganizer(1L, organizer(1L));
        stubShareCode("share-code-123");

        // when: 자유게임 생성을 수행한다.
        freeGameService.createFreeGame(1L, request);

        // then: 저장되는 FreeGame의 기본값이 FREE, NOT_STARTED여야 한다.
        ArgumentCaptor<FreeGame> captor = ArgumentCaptor.forClass(FreeGame.class);
        verify(gameRepository).save(captor.capture());

        FreeGame savedFreeGame = captor.getValue();
        assertThat(savedFreeGame.getGameType()).isEqualTo(GameType.FREE);
        assertThat(savedFreeGame.getGameStatus()).isEqualTo(GameStatus.NOT_STARTED);
    }

    @Test
    @DisplayName("managerIds가 2명을 초과하면 예외가 발생한다")
    void createFreeGame_withTooManyManagers_throwsException() {
        // given: managerIds가 3명인 자유게임 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임1")
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .courtCount(1)
                .roundCount(1)
                .managerIds(List.of(1L, 2L, 3L))
                .build();

        stubUserExists(1L);

        // when & then: managerIds가 2명을 초과하면 예외가 발생해야 한다.
        assertThatThrownBy(() -> freeGameService.createFreeGame(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("managerIds");
    }

    @Test
    @DisplayName("manager가 서비스 사용자가 아니면 예외가 발생한다")
    void createFreeGame_withUnknownManagerId_throwsException() {
        // given: 존재하지 않는 managerId가 포함된 자유게임 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .gradeType(GradeType.NATIONAL)
                .courtCount(1)
                .roundCount(1)
                .managerIds(List.of(2L))
                .build();

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.existsById(2L)).thenReturn(false);

        // when & then: 존재하지 않는 managerId면 예외가 발생하고 게임 저장은 호출되지 않아야 한다.
        assertThatThrownBy(() -> freeGameService.createFreeGame(1L, request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("동일 정보를 가진 참가자는 displayName을 접미사로 구분한다")
    void createFreeGame_withDuplicateParticipantDisplayName() {
        // given: 두 참가자의 기본 정보가 동일한 자유게임 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .gradeType(GradeType.NATIONAL)
                .courtCount(1)
                .roundCount(1)
                .participants(List.of(
                        ParticipantCreateRequest.builder()
                                .originalName("홍길동")
                                .gender(Gender.MALE)
                                .grade(Grade.A)
                                .ageGroup(20)
                                .build(),
                        ParticipantCreateRequest.builder()
                                .originalName("홍길동")
                                .gender(Gender.MALE)
                                .grade(Grade.A)
                                .ageGroup(20)
                                .build()
                ))
                .build();

        User organizer = organizer(1L);
        stubOrganizer(1L, organizer);
        stubShareCode("share-code-123");

        when(gameRepository.save(any(FreeGame.class)))
                .thenReturn(
                        FreeGame.builder()
                                .id(1L)
                                .title("자유게임")
                                .organizer(organizer)
                                .gradeType(GradeType.REGIONAL)
                                .gameType(GameType.FREE)
                                .gameStatus(GameStatus.NOT_STARTED)
                                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                                .shareCode("share-code-123")
                                .build()
                );
        when(gameParticipantRepository.save(any(GameParticipant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when: 자유게임 생성을 수행한다.
        freeGameService.createFreeGame(1L, request);

        // then: displayName이 서로 다르게 저장되어야 한다.
        ArgumentCaptor<GameParticipant> captor = ArgumentCaptor.forClass(GameParticipant.class);
        verify(gameParticipantRepository, times(2)).save(captor.capture());

        List<GameParticipant> saved = captor.getAllValues();
        assertThat(saved.get(0).getDisplayName()).isEqualTo("홍길동");
        assertThat(saved.get(1).getDisplayName()).isEqualTo("홍길동A");
    }

    @Test
    @DisplayName("자유게임 생성 시 참여자 목록이 있으면 참가자 저장을 호출한다")
    void createFreeGame_withParticipants_callsSaveParticipants() {
        // given: 참여자 목록이 포함된 자유게임 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(2)
                .participants(
                        List.of(
                                ParticipantCreateRequest.builder()
                                        .originalName("박지성")
                                        .gender(Gender.MALE)
                                        .grade(Grade.A)
                                        .ageGroup(20)
                                        .build(),
                                ParticipantCreateRequest.builder()
                                        .originalName("손흥민")
                                        .gender(Gender.MALE)
                                        .grade(Grade.A)
                                        .ageGroup(20)
                                        .build()
                        )
                )
                .build();

        User organizer = organizer(1L);
        stubOrganizer(1L, organizer);
        stubShareCode("share-code-123");
        when(gameRepository.save(any(FreeGame.class)))
                .thenReturn(
                        FreeGame.builder()
                                .id(1L)
                                .title("자유게임")
                                .organizer(organizer)
                                .gradeType(GradeType.NATIONAL)
                                .gameType(GameType.FREE)
                                .gameStatus(GameStatus.NOT_STARTED)
                                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                                .shareCode("share-code-123")
                                .build()
                );

        // when: 자유게임 생성을 수행한다.
        freeGameService.createFreeGame(1L, request);

        // then: 참가자 저장이 참가자 수만큼 호출되어야 한다.
        verify(gameParticipantRepository, times(2)).save(any(GameParticipant.class));
    }

    @Test
    @DisplayName("자유게임 생성 시 organizer가 저장된다")
    void createFreeGame_setsOrganizerId() {
        // given: 게임을 생성한 user가 organizer로 저장되어야 하는 요청을 준비한다.
        Long userId = 1L;

        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(3)
                .build();

        User organizer = organizer(userId);
        stubOrganizer(userId, organizer);
        stubShareCode("share-code-123");
        when(gameRepository.save(any(FreeGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when: 자유게임 생성을 수행한다.
        freeGameService.createFreeGame(userId, request);

        // then: 저장되는 FreeGame의 organizer는 요청한 user여야 한다.
        ArgumentCaptor<FreeGame> captor = ArgumentCaptor.forClass(FreeGame.class);
        verify(gameRepository).save(captor.capture());

        FreeGame saved = captor.getValue();
        assertThat(saved.getOrganizer()).isSameAs(organizer);
        assertThat(saved.getOrganizer().getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("자유게임 생성 시 free game setting 저장을 호출한다")
    void createFreeGame_savesFreeGameSetting() {
        // given: courtCount와 roundCount가 포함된 자유게임 생성 요청을 준비한다.
        Long userId = 1L;
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(3)
                .build();

        User organizer = organizer(userId);
        stubOrganizer(userId, organizer);
        stubShareCode("share-code-123");

        FreeGame savedFreeGame = FreeGame.builder()
                .title("자유게임")
                .organizer(organizer)
                .gradeType(GradeType.NATIONAL)
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .shareCode("share-code-123")
                .build();

        when(gameRepository.save(any(FreeGame.class))).thenReturn(savedFreeGame);

        // when: 자유게임 생성을 수행한다.
        freeGameService.createFreeGame(userId, request);

        // then: FreeGameSetting 저장 시 courtCount, roundCount, freeGame 참조가 반영되어야 한다.
        ArgumentCaptor<FreeGameSetting> captor = ArgumentCaptor.forClass(FreeGameSetting.class);
        verify(freeGameSettingRepository).save(captor.capture());

        FreeGameSetting savedSetting = captor.getValue();
        assertThat(savedSetting.getCourtCount()).isEqualTo(request.getCourtCount());
        assertThat(savedSetting.getRoundCount()).isEqualTo(request.getRoundCount());
        assertThat(savedSetting.getFreeGame()).isEqualTo(savedFreeGame);
    }

    @Test
    @DisplayName("자유게임 생성 시 location이 있으면 저장한다")
    void createFreeGame_withLocation_savesLocation() {
        // given: location이 포함된 자유게임 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("주말 자유게임")
                .location("잠실 배드민턴장")
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(3)
                .build();

        User organizer = organizer(1L);
        stubOrganizer(1L, organizer);
        when(gameRepository.save(any(FreeGame.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubShareCode("share-code");

        // when: 자유게임 생성을 수행한다.
        freeGameService.createFreeGame(1L, request);

        // then: 저장되는 FreeGame 엔티티에 location이 반영되어야 한다.
        FreeGame savedFreeGame = savedGameCaptor().getValue();
        assertThat(savedFreeGame.getLocation()).isEqualTo("잠실 배드민턴장");
    }
}
