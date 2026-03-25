package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameParticipantRepository;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityAccountRepository;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

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
    private IdentityAccountRepository identityAccountRepository;
    private AddGameParticipantPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        gameParticipantRepository = mock(GameParticipantRepository.class);
        identityAccountRepository = mock(IdentityAccountRepository.class);
        adapter = new AddGameParticipantPersistenceAdapter(gameParticipantRepository, identityAccountRepository);
    }

    @Test
    @DisplayName("회원 참가자 추가 시 identityAccountId를 연결한다")
    void add_withUserId_loadsAndAssignsIdentityAccountId() {
        UUID identityAccountId = UUID.randomUUID();
        FreeGame freeGame = freeGame();
        AddFreeGameParticipantCommand command =
                new AddFreeGameParticipantCommand(identityAccountId, "서승재", Gender.MALE, Grade.A, 20);

        given(identityAccountRepository.findById(identityAccountId)).willReturn(Optional.of(mock()));
        given(gameParticipantRepository.findByFreeGameId(freeGame.getId())).willReturn(List.of());
        given(gameParticipantRepository.save(any(GameParticipant.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        adapter.add(freeGame, command);

        ArgumentCaptor<GameParticipant> captor = ArgumentCaptor.forClass(GameParticipant.class);
        verify(gameParticipantRepository).save(captor.capture());
        assertThat(captor.getValue().getIdentityAccountId()).isEqualTo(identityAccountId);
        assertThat(captor.getValue().getDisplayName()).isEqualTo("서승재");
    }

    @Test
    @DisplayName("존재하지 않는 identityAccountId면 예외가 발생한다")
    void add_withUnknownUserId_throws() {
        UUID identityAccountId = UUID.randomUUID();
        FreeGame freeGame = freeGame();
        AddFreeGameParticipantCommand command =
                new AddFreeGameParticipantCommand(identityAccountId, "서승재", Gender.MALE, Grade.A, 20);

        given(identityAccountRepository.findById(identityAccountId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.add(freeGame, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identityAccountId");
    }

    @Test
    @DisplayName("동일 정보를 가진 기존 참가자가 있으면 displayName 접미사를 부여한다")
    void add_withDuplicateParticipant_assignsDistinctDisplayName() {
        FreeGame freeGame = freeGame();
        AddFreeGameParticipantCommand command =
                new AddFreeGameParticipantCommand(null, "홍길동", Gender.MALE, Grade.A, 20);

        GameParticipant existing = GameParticipant.create(
                freeGame,
                null,
                "홍길동",
                "홍길동",
                Gender.MALE,
                Grade.A,
                20
        );
        ReflectionTestUtils.setField(existing, "id", UUID.randomUUID());

        given(gameParticipantRepository.findByFreeGameId(freeGame.getId())).willReturn(List.of(existing));
        given(gameParticipantRepository.save(any(GameParticipant.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        GameParticipant saved = adapter.add(freeGame, command);

        assertThat(saved.getDisplayName()).isEqualTo("홍길동A");
    }

    private FreeGame freeGame() {
        FreeGame freeGame = FreeGame.create(
                "자유게임",
                UUID.randomUUID(),
                GradeType.NATIONAL,
                com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode.STATUS_ONLY,
                null,
                null
        );
        ReflectionTestUtils.setField(freeGame, "id", UUID.randomUUID());
        return freeGame;
    }
}
