package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AddGameParticipantPersistenceAdapterTest {

    private GameParticipantRepository gameParticipantRepository;
    private UserRepository userRepository;
    private AddGameParticipantPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        gameParticipantRepository = mock(GameParticipantRepository.class);
        userRepository = mock(UserRepository.class);
        adapter = new AddGameParticipantPersistenceAdapter(gameParticipantRepository, userRepository);
    }

    @Test
    @DisplayName("회원 참가자 추가 시 userId로 사용자를 연결한다")
    void add_withUserId_loadsAndAssignsUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        FreeGame freeGame = FreeGame.builder().id(UUID.randomUUID()).title("자유게임").build();
        AddFreeGameParticipantCommand command =
                new AddFreeGameParticipantCommand(userId, "서승재", Gender.MALE, Grade.A, 20);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(gameParticipantRepository.findByFreeGameId(freeGame.getId())).willReturn(List.of());
        given(gameParticipantRepository.save(any(GameParticipant.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        adapter.add(freeGame, command);

        ArgumentCaptor<GameParticipant> captor = ArgumentCaptor.forClass(GameParticipant.class);
        verify(gameParticipantRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getDisplayName()).isEqualTo("서승재");
    }

    @Test
    @DisplayName("존재하지 않는 userId면 예외가 발생한다")
    void add_withUnknownUserId_throws() {
        UUID userId = UUID.randomUUID();
        FreeGame freeGame = FreeGame.builder().id(UUID.randomUUID()).title("자유게임").build();
        AddFreeGameParticipantCommand command =
                new AddFreeGameParticipantCommand(userId, "서승재", Gender.MALE, Grade.A, 20);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.add(freeGame, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 userId");
    }

    @Test
    @DisplayName("동일 정보를 가진 기존 참가자가 있으면 displayName 접미사를 부여한다")
    void add_withDuplicateParticipant_assignsDistinctDisplayName() {
        FreeGame freeGame = FreeGame.builder().id(UUID.randomUUID()).title("자유게임").build();
        AddFreeGameParticipantCommand command =
                new AddFreeGameParticipantCommand(null, "홍길동", Gender.MALE, Grade.A, 20);

        GameParticipant existing = GameParticipant.builder()
                .id(UUID.randomUUID())
                .freeGame(freeGame)
                .originalName("홍길동")
                .displayName("홍길동")
                .gender(Gender.MALE)
                .grade(Grade.A)
                .ageGroup(20)
                .build();

        given(gameParticipantRepository.findByFreeGameId(freeGame.getId())).willReturn(List.of(existing));
        given(gameParticipantRepository.save(any(GameParticipant.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        GameParticipant saved = adapter.add(freeGame, command);

        assertThat(saved.getDisplayName()).isEqualTo("홍길동A");
    }
}
