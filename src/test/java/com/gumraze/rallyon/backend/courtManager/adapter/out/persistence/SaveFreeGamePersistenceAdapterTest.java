package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.repository.GameRepository;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import com.gumraze.rallyon.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class SaveFreeGamePersistenceAdapterTest {

    private GameRepository gameRepository;
    private SaveFreeGamePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        gameRepository = mock(GameRepository.class);
        adapter = new SaveFreeGamePersistenceAdapter(gameRepository);
    }

    @Test
    @DisplayName("자유게임을 저장한다.")
    void save_returnsSavedFreeGame() {
        // given
        User organizer = User.builder().id(1L).build();
        UUID gameId = UUID.randomUUID();

        FreeGame freeGame = FreeGame.builder()
                .title("자유게임")
                .organizer(organizer)
                .gradeType(GradeType.NATIONAL)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .location("잠실 배드민턴장")
                .build();

        FreeGame savedFreeGame = FreeGame.builder()
                .id(gameId)
                .title("자유게임")
                .organizer(organizer)
                .gradeType(GradeType.NATIONAL)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .location("잠실 배드민턴장")
                .build();

        given(gameRepository.save(same(freeGame))).willReturn(savedFreeGame);

        // when
        FreeGame result = adapter.save(freeGame);


        // then
        assertThat(result).isSameAs(savedFreeGame);
        assertThat(result.getId()).isEqualTo(savedFreeGame.getId());
    }
}
