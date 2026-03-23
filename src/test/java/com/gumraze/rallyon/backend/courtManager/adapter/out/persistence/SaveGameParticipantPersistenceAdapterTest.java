package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameParticipantRepository;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SaveGameParticipantPersistenceAdapterTest {

    private GameParticipantRepository gameParticipantRepository;
    private UserRepository userRepository;
    private SaveGameParticipantPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        gameParticipantRepository = mock(GameParticipantRepository.class);
        userRepository = mock(UserRepository.class);
        adapter = new SaveGameParticipantPersistenceAdapter(gameParticipantRepository, userRepository);
    }

    @Test
    @DisplayName("참가자를 저장하고 clientId 기준 매핑을 반환한다")
    void saveAll_returnsParticipantsByClientId() {
        // given
        FreeGame freeGame = FreeGame.builder()
                .id(UUID.randomUUID())
                .title("자유게임")
                .build();

        List<CreateFreeGameCommand.Participant> participants = List.of(
                new CreateFreeGameCommand.Participant("p1", null, "서승재", Gender.MALE, Grade.SS, 20),
                new CreateFreeGameCommand.Participant("p2", null, "김원호", Gender.MALE, Grade.SS, 20)
        );

        AtomicInteger sequence = new AtomicInteger(1);
        given(gameParticipantRepository.save(any(GameParticipant.class)))
                .willAnswer(invocation -> {
                    GameParticipant participant = invocation.getArgument(0);
                    UUID participantId = UUID.fromString(
                            String.format("018f1a1e-2b2f-7c11-9a55-%012d", sequence.getAndIncrement())
                    );
                    ReflectionTestUtils.setField(participant, "id", participantId);
                    return participant;
                });

        // when
        Map<String, GameParticipant> result = adapter.saveAll(freeGame, participants);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("p1", "p2");
        assertThat(result.get("p1").getOriginalName()).isEqualTo("서승재");
        assertThat(result.get("p2").getOriginalName()).isEqualTo("김원호");
    }

    @Test
    @DisplayName("userId가 있으면 회원 참가자로 저장한다")
    void saveAll_withUserId_loadsAndAssignsUser() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        FreeGame freeGame = FreeGame.builder().id(UUID.randomUUID()).title("자유게임").build();
        List<CreateFreeGameCommand.Participant> participants = List.of(
                new CreateFreeGameCommand.Participant("p1", userId, "서승재", Gender.MALE, Grade.A, 20)
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(gameParticipantRepository.save(any(GameParticipant.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        adapter.saveAll(freeGame, participants);

        // then
        ArgumentCaptor<GameParticipant> captor = ArgumentCaptor.forClass(GameParticipant.class);
        verify(gameParticipantRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("존재하지 않는 userId면 예외가 발생한다")
    void saveAll_withUnknownUserId_throws() {
        // given
        UUID userId = UUID.randomUUID();
        FreeGame freeGame = FreeGame.builder().id(UUID.randomUUID()).title("자유게임").build();
        List<CreateFreeGameCommand.Participant> participants = List.of(
                new CreateFreeGameCommand.Participant("p1", userId, "서승재", Gender.MALE, Grade.A, 20)
        );

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adapter.saveAll(freeGame, participants))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 userId");
    }

    @Test
    @DisplayName("동일 정보를 가진 참가자는 displayName 접미사로 구분한다")
    void saveAll_withDuplicateParticipants_assignsDistinctDisplayNames() {
        // given
        FreeGame freeGame = FreeGame.builder().id(UUID.randomUUID()).title("자유게임").build();
        List<CreateFreeGameCommand.Participant> participants = List.of(
                new CreateFreeGameCommand.Participant("p1", null, "홍길동", Gender.MALE, Grade.A, 20),
                new CreateFreeGameCommand.Participant("p2", null, "홍길동", Gender.MALE, Grade.A, 20)
        );

        given(gameParticipantRepository.save(any(GameParticipant.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        adapter.saveAll(freeGame, participants);

        // then
        ArgumentCaptor<GameParticipant> captor = ArgumentCaptor.forClass(GameParticipant.class);
        verify(gameParticipantRepository, times(2)).save(captor.capture());
        List<GameParticipant> savedParticipants = captor.getAllValues();

        assertThat(savedParticipants.get(0).getDisplayName()).isEqualTo("홍길동");
        assertThat(savedParticipants.get(1).getDisplayName()).isEqualTo("홍길동A");
    }
}
