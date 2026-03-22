package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import org.springframework.stereotype.Component;

@Component
public interface AddGameParticipantPort {
    GameParticipant add(FreeGame freeGame, AddFreeGameParticipantCommand command);
}
