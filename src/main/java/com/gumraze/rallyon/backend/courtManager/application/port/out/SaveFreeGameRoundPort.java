package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.domain.assignment.RoundAssignment;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import java.util.List;

public interface SaveFreeGameRoundPort {

    void saveAll(FreeGame freeGame, List<RoundAssignment> roundAssignments);

}
