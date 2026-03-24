package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameInfoCommand;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameResponse;

public interface UpdateFreeGameInfoUseCase {

    UpdateFreeGameResponse update(UpdateFreeGameInfoCommand command);
}
