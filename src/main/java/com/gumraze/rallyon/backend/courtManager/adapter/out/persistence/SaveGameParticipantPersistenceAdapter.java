package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.repository.GameParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SaveGameParticipantPersistenceAdapter implements SaveGameParticipantPort {

    private final GameParticipantRepository gameParticipantRepository;

    @Override
    public Map<String, GameParticipant> saveAll(
            FreeGame freeGame,
            List<CreateFreeGameCommand.Participant> participants
    ) {
        if (participants == null || participants.isEmpty()) {
            return Map.of();
        }

        Map<String, GameParticipant> participantsByClientId = new LinkedHashMap<>();

        for (CreateFreeGameCommand.Participant participant : participants) {
            GameParticipant toSave = GameParticipant.builder()
                    .freeGame(freeGame)
                    .user(null)
                    .originalName(participant.originalName())
                    .displayName(participant.originalName())
                    .gender(participant.gender())
                    .grade(participant.grade())
                    .ageGroup(participant.ageGroup())
                    .build();

            GameParticipant savedParticipant = gameParticipantRepository.save(toSave);
            participantsByClientId.put(participant.clientId(), savedParticipant);
        }
        return participantsByClientId;
    }
}
