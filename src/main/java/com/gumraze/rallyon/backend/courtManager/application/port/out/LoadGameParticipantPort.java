package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadGameParticipantPort {

    List<GameParticipant> loadParticipantsByGameId(UUID gameId);

    Optional<GameParticipant> loadParticipantById(UUID participantId);
}
