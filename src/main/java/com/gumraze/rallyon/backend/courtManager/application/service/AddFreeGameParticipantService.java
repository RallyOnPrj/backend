package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.AddGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddFreeGameParticipantService {

    private final LoadFreeGamePort loadFreeGamePort;
    private final AddGameParticipantPort addGameParticipantPort;

    public UUID add(UUID organizerId, UUID gameId, AddFreeGameParticipantCommand command) {
        FreeGame freeGame = loadFreeGamePort.loadById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자유게임입니다."));

        if (!freeGame.getOrganizer().getId().equals(organizerId)) {
            throw new IllegalArgumentException("자신이 운영하는 자유게임에만 참가자를 추가할 수 있습니다.");
        }

        GameParticipant savedParticipant = addGameParticipantPort.add(freeGame, command);
        return savedParticipant.getId();
    }
}
