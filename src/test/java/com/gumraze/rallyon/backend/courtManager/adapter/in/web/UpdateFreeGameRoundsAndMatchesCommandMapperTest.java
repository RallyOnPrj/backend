package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameRoundsAndMatchesCommand;
import com.gumraze.rallyon.backend.courtManager.dto.MatchRequest;
import com.gumraze.rallyon.backend.courtManager.dto.RoundRequest;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRoundMatchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateFreeGameRoundsAndMatchesCommandMapperTest {

    private final UpdateFreeGameRoundsAndMatchesCommandMapper mapper = new UpdateFreeGameRoundsAndMatchesCommandMapper();

    @Test
    @DisplayName("라운드/매치 수정 request를 nested command로 변환한다")
    void toCommand_maps_nested_rounds_and_matches() {
        UUID organizerIdentityAccountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        UpdateFreeGameRoundMatchRequest request = new UpdateFreeGameRoundMatchRequest(
                List.of(
                        new RoundRequest(
                                1,
                                List.of(
                                        new MatchRequest(1, Arrays.asList(participant1, null), Arrays.asList(participant2, null))
                                )
                        )
                )
        );

        UpdateFreeGameRoundsAndMatchesCommand command = mapper.toCommand(organizerIdentityAccountId, gameId, request);

        assertThat(command.organizerId()).isEqualTo(organizerIdentityAccountId);
        assertThat(command.gameId()).isEqualTo(gameId);
        assertThat(command.rounds()).hasSize(1);
        assertThat(command.rounds().getFirst().roundNumber()).isEqualTo(1);
        assertThat(command.rounds().getFirst().matches()).hasSize(1);
        assertThat(command.rounds().getFirst().matches().getFirst().courtNumber()).isEqualTo(1);
        assertThat(command.rounds().getFirst().matches().getFirst().teamAIds()).containsExactly(participant1, null);
        assertThat(command.rounds().getFirst().matches().getFirst().teamBIds()).containsExactly(participant2, null);
    }

    @Test
    @DisplayName("rounds가 null이면 command rounds도 null이다")
    void toCommand_keeps_null_rounds() {
        UUID organizerIdentityAccountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        UpdateFreeGameRoundMatchRequest request = new UpdateFreeGameRoundMatchRequest(null);

        UpdateFreeGameRoundsAndMatchesCommand command = mapper.toCommand(organizerIdentityAccountId, gameId, request);

        assertThat(command.rounds()).isNull();
    }
}
