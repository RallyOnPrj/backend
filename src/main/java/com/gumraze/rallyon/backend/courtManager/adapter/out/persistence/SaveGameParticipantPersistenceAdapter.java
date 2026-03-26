package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.domain.ParticipantDisplayNamePolicy;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameParticipantRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SaveGameParticipantPersistenceAdapter implements SaveGameParticipantPort {

    private final GameParticipantRepository gameParticipantRepository;
    private final IdentityAccountRepository identityAccountRepository;

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
            if (participant.identityAccountId() != null) {
                identityAccountRepository.findById(participant.identityAccountId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("존재하지 않는 identityAccountId입니다. :" + participant.identityAccountId()));
            }

            GameParticipant toSave = GameParticipant.create(
                    freeGame,
                    participant.identityAccountId(),
                    participant.originalName(),
                    ParticipantDisplayNamePolicy.resolve(
                            participant.originalName(),
                            participant.gender(),
                            participant.grade(),
                            participant.ageGroup(),
                            participantsByClientId.values()
                    ),
                    participant.gender(),
                    participant.grade(),
                    participant.ageGroup()
            );

            GameParticipant savedParticipant = gameParticipantRepository.save(toSave);
            participantsByClientId.put(participant.clientId(), savedParticipant);
        }
        return participantsByClientId;
    }
}
