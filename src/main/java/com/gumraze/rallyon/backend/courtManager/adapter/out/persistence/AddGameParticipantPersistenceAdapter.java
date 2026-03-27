package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.AddGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.domain.ParticipantDisplayNamePolicy;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameParticipantRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddGameParticipantPersistenceAdapter implements AddGameParticipantPort {

    private final GameParticipantRepository gameParticipantRepository;
    private final AccountRepository accountRepository;

    @Override
    public GameParticipant add(FreeGame freeGame, AddFreeGameParticipantCommand command) {
        if (command.accountId() != null) {
            accountRepository.findById(command.accountId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 accountId입니다. :" + command.accountId()));
        }

        GameParticipant participant = GameParticipant.create(
                freeGame,
                command.accountId(),
                command.name(),
                ParticipantDisplayNamePolicy.resolve(
                        command.name(),
                        command.gender(),
                        command.grade(),
                        command.age(),
                        gameParticipantRepository.findByFreeGameId(freeGame.getId())
                ),
                command.gender(),
                command.grade(),
                command.age()
        );

        return gameParticipantRepository.save(participant);
    }
}
