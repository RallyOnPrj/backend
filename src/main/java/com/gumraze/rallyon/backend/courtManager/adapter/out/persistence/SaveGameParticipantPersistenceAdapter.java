package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.repository.GameParticipantRepository;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SaveGameParticipantPersistenceAdapter implements SaveGameParticipantPort {

    private final GameParticipantRepository gameParticipantRepository;
    private final UserRepository userRepository;

    @Override
    public Map<String, GameParticipant> saveAll(
            FreeGame freeGame,
            List<CreateFreeGameCommand.Participant> participants
    ) {
        if (participants == null || participants.isEmpty()) {
            return Map.of();
        }

        Map<ParticipantKey, Integer> duplicateCount = new HashMap<>();
        Map<String, GameParticipant> participantsByClientId = new LinkedHashMap<>();

        for (CreateFreeGameCommand.Participant participant : participants) {
            ParticipantKey key = new ParticipantKey(
                    participant.originalName(),
                    participant.gender(),
                    participant.grade(),
                    participant.ageGroup()
            );

            int count = duplicateCount.getOrDefault(key, 0);
            String displayName = count == 0
                    ? participant.originalName()
                    : participant.originalName() + suffix(count);
            duplicateCount.put(key, count + 1);

            User participantUser = null;
            if (participant.userId() != null) {
                participantUser = userRepository.findById(participant.userId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("존재하지 않는 userId입니다. :" + participant.userId()));
            }

            GameParticipant toSave = GameParticipant.builder()
                    .freeGame(freeGame)
                    .user(participantUser)
                    .originalName(participant.originalName())
                    .displayName(displayName)
                    .gender(participant.gender())
                    .grade(participant.grade())
                    .ageGroup(participant.ageGroup())
                    .build();

            GameParticipant savedParticipant = gameParticipantRepository.save(toSave);
            participantsByClientId.put(participant.clientId(), savedParticipant);
        }
        return participantsByClientId;
    }

    private String suffix(int count) {
        return String.valueOf((char) ('A' + count - 1));
    }

    private record ParticipantKey(
            String originalName,
            Gender gender,
            Grade grade,
            Integer ageGroup
    ) {
    }
}
