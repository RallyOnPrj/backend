package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.repository.GameRepository;
import com.gumraze.rallyon.backend.courtManager.service.ShareCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class IssueShareCodePersistenceAdapterTest {

    private ShareCodeGenerator shareCodeGenerator;
    private GameRepository gameRepository;
    private IssueShareCodePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        shareCodeGenerator = mock(ShareCodeGenerator.class);
        gameRepository = mock(GameRepository.class);
        adapter = new IssueShareCodePersistenceAdapter(shareCodeGenerator, gameRepository);
    }

    @Test
    @DisplayName("shareCode가 유일하면 그대로 발급한다")
    void issue_returnsGeneratedShareCode() {
        // given
        String shareCode = "generated-share-code";
        given(shareCodeGenerator.generate()).willReturn(shareCode);
        given(gameRepository.existsByShareCode(shareCode)).willReturn(false);

        // when
        String result = adapter.issue();

        // then
        assertThat(result).isEqualTo(shareCode);
    }

    @Test
    @DisplayName("shareCode가 충돌하면 새 코드를 다시 발급한다")
    void issue_regeneratesWhenShareCodeCollides() {
        // given
        String firstShareCode = "first-share-code";
        String secondShareCode = "second-share-code";
        given(shareCodeGenerator.generate()).willReturn(firstShareCode, secondShareCode);
        given(gameRepository.existsByShareCode(firstShareCode)).willReturn(true);
        given(gameRepository.existsByShareCode(secondShareCode)).willReturn(false);

        // when
        String result = adapter.issue();

        // then
        assertThat(result).isEqualTo(secondShareCode);
    }

    @Test
    @DisplayName("shareCode 충돌이 최대 재시도를 초과하면 예외가 발생한다")
    void issue_throwsWhenCollisionsExceedMaxRetries() {
        // given
        String shareCode = "always-colliding-share-code";
        given(shareCodeGenerator.generate()).willReturn(shareCode);
        given(gameRepository.existsByShareCode(shareCode)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adapter.issue())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shareCode");
    }
}
