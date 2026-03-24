package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.AddGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.domain.ParticipantDisplayNamePolicy;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameParticipantRepository;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddGameParticipantPersistenceAdapter implements AddGameParticipantPort {

    private final GameParticipantRepository gameParticipantRepository;
    private final UserRepository userRepository;

    @Override
    public GameParticipant add(FreeGame freeGame, AddFreeGameParticipantCommand command) {
        User participantUser = null;
        if (command.userId() != null) {
            participantUser = userRepository.findById(command.userId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("존재하지 않는 userId입니다. :" + command.userId()));
        }

        GameParticipant participant = GameParticipant.builder()
                .freeGame(freeGame)
                .user(participantUser)
                .originalName(command.name())
                .displayName(ParticipantDisplayNamePolicy.resolve(
                        command.name(),
                        command.gender(),
                        command.grade(),
                        command.age(),
                        gameParticipantRepository.findByFreeGameId(freeGame.getId())
                ))
                .gender(command.gender())
                .grade(command.grade())
                .ageGroup(command.age())
                .build();

        return gameParticipantRepository.save(participant);
    }
}
