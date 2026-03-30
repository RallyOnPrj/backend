package com.gumraze.rallyon.backend.courtManager.domain;

import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParticipantDisplayNamePolicyTest {

    @Test
    @DisplayName("동일 조건 참가자가 없으면 원래 이름을 그대로 사용한다")
    void resolve_returns_original_name_when_duplicate_does_not_exist() {
        String displayName = ParticipantDisplayNamePolicy.resolve(
                "서승재",
                Gender.MALE,
                Grade.A,
                20,
                List.of()
        );

        assertThat(displayName).isEqualTo("서승재");
    }

    @Test
    @DisplayName("동일 조건 참가자가 있으면 suffix를 붙인다")
    void resolve_appends_suffix_when_duplicate_exists() {
        GameParticipant first = mock(GameParticipant.class);
        when(first.getOriginalName()).thenReturn("서승재");
        when(first.getGender()).thenReturn(Gender.MALE);
        when(first.getGrade()).thenReturn(Grade.A);
        when(first.getAgeGroup()).thenReturn(20);

        GameParticipant second = mock(GameParticipant.class);
        when(second.getOriginalName()).thenReturn("서승재");
        when(second.getGender()).thenReturn(Gender.MALE);
        when(second.getGrade()).thenReturn(Grade.A);
        when(second.getAgeGroup()).thenReturn(20);

        String displayName = ParticipantDisplayNamePolicy.resolve(
                "서승재",
                Gender.MALE,
                Grade.A,
                20,
                List.of(first, second)
        );

        assertThat(displayName).isEqualTo("서승재B");
    }
}
