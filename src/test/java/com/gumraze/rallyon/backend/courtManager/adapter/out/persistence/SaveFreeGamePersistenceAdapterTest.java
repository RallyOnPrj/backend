package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameRepository;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class SaveFreeGamePersistenceAdapterTest {

    private GameRepository gameRepository;
    private SaveFreeGamePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        gameRepository = mock(GameRepository.class);
        adapter = new SaveFreeGamePersistenceAdapter(gameRepository);
    }

    @Test
    @DisplayName("자유게임을 저장한다")
    void save_returnsSavedFreeGame() {
        UUID organizerIdentityAccountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        FreeGame freeGame = FreeGame.create(
                "자유게임",
                organizerIdentityAccountId,
                GradeType.NATIONAL,
                MatchRecordMode.STATUS_ONLY,
                null,
                "잠실 배드민턴장"
        );

        FreeGame savedFreeGame = FreeGame.create(
                "자유게임",
                organizerIdentityAccountId,
                GradeType.NATIONAL,
                MatchRecordMode.STATUS_ONLY,
                null,
                "잠실 배드민턴장"
        );
        ReflectionTestUtils.setField(savedFreeGame, "id", gameId);

        given(gameRepository.save(same(freeGame))).willReturn(savedFreeGame);

        FreeGame result = adapter.save(freeGame);

        assertThat(result).isSameAs(savedFreeGame);
        assertThat(result.getId()).isEqualTo(savedFreeGame.getId());
    }
}
