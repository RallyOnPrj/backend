package com.gumraze.rallyon.backend.courtManager.domain;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameRoundsAndMatchesCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoundMatchValidationPolicyTest {

    @Test
    @DisplayName("null 슬롯이 포함된 유효한 라운드 구성은 허용한다")
    void validate_allows_sparse_valid_rounds() {
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();

        List<UpdateFreeGameRoundsAndMatchesCommand.Round> rounds = List.of(
                round(1, List.of(
                        match(1, Arrays.asList(participant1, null), List.of(participant2, participant3))
                ))
        );

        assertThatCode(() -> RoundMatchValidationPolicy.validate(rounds, Set.of(participant1, participant2, participant3)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("게임에 속하지 않은 participantId가 있으면 예외가 발생한다")
    void validate_throws_when_participant_is_not_in_game() {
        UUID participant1 = UUID.randomUUID();
        UUID outsider = UUID.randomUUID();

        List<UpdateFreeGameRoundsAndMatchesCommand.Round> rounds = List.of(
                round(1, List.of(
                        match(1, Arrays.asList(participant1, null), Arrays.asList(outsider, null))
                ))
        );

        assertThatThrownBy(() -> RoundMatchValidationPolicy.validate(rounds, Set.of(participant1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않거나 해당 게임에 속하지 않는 participantId입니다.");
    }

    @Test
    @DisplayName("같은 매치 내 중복 participantId는 예외가 발생한다")
    void validate_throws_when_duplicate_in_same_match() {
        UUID participant1 = UUID.randomUUID();

        List<UpdateFreeGameRoundsAndMatchesCommand.Round> rounds = List.of(
                round(1, List.of(
                        match(1, List.of(participant1, participant1), Arrays.asList(null, null))
                ))
        );

        assertThatThrownBy(() -> RoundMatchValidationPolicy.validate(rounds, Set.of(participant1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("match 내 participantId 중복입니다.");
    }

    @Test
    @DisplayName("같은 라운드의 다른 코트에 같은 participantId가 있으면 예외가 발생한다")
    void validate_throws_when_duplicate_in_same_round() {
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();

        List<UpdateFreeGameRoundsAndMatchesCommand.Round> rounds = List.of(
                round(1, List.of(
                        match(1, Arrays.asList(participant1, null), Arrays.asList(participant2, null)),
                        match(2, Arrays.asList(participant1, null), Arrays.asList(participant3, null))
                ))
        );

        assertThatThrownBy(() -> RoundMatchValidationPolicy.validate(rounds, Set.of(participant1, participant2, participant3)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("round 내 participantId 중복입니다.");
    }

    @Test
    @DisplayName("courtNumber는 1부터 연속이어야 한다")
    void validate_throws_when_court_numbers_are_not_consecutive() {
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();
        UUID participant4 = UUID.randomUUID();

        List<UpdateFreeGameRoundsAndMatchesCommand.Round> rounds = List.of(
                round(1, List.of(
                        match(1, Arrays.asList(participant1, null), Arrays.asList(participant2, null)),
                        match(3, Arrays.asList(participant3, null), Arrays.asList(participant4, null))
                ))
        );

        assertThatThrownBy(() -> RoundMatchValidationPolicy.validate(
                rounds,
                Set.of(participant1, participant2, participant3, participant4)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("courtNumber는 1..n 연속이어야 합니다.");
    }

    private UpdateFreeGameRoundsAndMatchesCommand.Round round(
            int roundNumber,
            List<UpdateFreeGameRoundsAndMatchesCommand.Match> matches
    ) {
        return new UpdateFreeGameRoundsAndMatchesCommand.Round(roundNumber, matches);
    }

    private UpdateFreeGameRoundsAndMatchesCommand.Match match(
            int courtNumber,
            List<UUID> teamAIds,
            List<UUID> teamBIds
    ) {
        return new UpdateFreeGameRoundsAndMatchesCommand.Match(courtNumber, teamAIds, teamBIds);
    }
}
