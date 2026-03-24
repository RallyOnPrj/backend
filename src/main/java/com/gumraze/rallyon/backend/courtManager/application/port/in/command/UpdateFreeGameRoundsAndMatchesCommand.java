package com.gumraze.rallyon.backend.courtManager.application.port.in.command;

import java.util.List;
import java.util.UUID;

public record UpdateFreeGameRoundsAndMatchesCommand(
        UUID organizerId,
        UUID gameId,
        List<Round> rounds
) {
    public record Round(
            Integer roundNumber,
            List<Match> matches
    ) {
    }

    public record Match(
            Integer courtNumber,
            List<UUID> teamAIds,
            List<UUID> teamBIds
    ) {
    }
}
