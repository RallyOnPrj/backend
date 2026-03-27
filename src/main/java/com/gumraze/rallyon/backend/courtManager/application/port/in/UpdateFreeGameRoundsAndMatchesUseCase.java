package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameRoundsAndMatchesCommand;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRoundMatchResponse;

public interface UpdateFreeGameRoundsAndMatchesUseCase {

    UpdateFreeGameRoundMatchResponse update(UpdateFreeGameRoundsAndMatchesCommand command);
}
