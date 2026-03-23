package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameRoundsAndMatchesCommand;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRoundMatchRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UpdateFreeGameRoundsAndMatchesCommandMapper {

    public UpdateFreeGameRoundsAndMatchesCommand toCommand(
            UUID organizerId,
            UUID gameId,
            UpdateFreeGameRoundMatchRequest request
    ) {
        return new UpdateFreeGameRoundsAndMatchesCommand(
                organizerId,
                gameId,
                request.getRounds() == null ? null : request.getRounds().stream()
                        .map(round -> new UpdateFreeGameRoundsAndMatchesCommand.Round(
                                round.getRoundNumber(),
                                round.getMatches().stream()
                                        .map(match -> new UpdateFreeGameRoundsAndMatchesCommand.Match(
                                                match.getCourtNumber(),
                                                match.getTeamAIds(),
                                                match.getTeamBIds()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }
}
