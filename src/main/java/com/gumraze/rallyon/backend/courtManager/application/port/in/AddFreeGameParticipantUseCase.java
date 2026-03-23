package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;

import java.util.UUID;

public interface AddFreeGameParticipantUseCase {

    UUID add(UUID organizerId, UUID gameId, AddFreeGameParticipantCommand command);
}
