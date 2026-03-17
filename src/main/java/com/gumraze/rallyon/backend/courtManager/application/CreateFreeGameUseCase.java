package com.gumraze.rallyon.backend.courtManager.application;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import java.util.UUID;

public interface CreateFreeGameUseCase {

    UUID create(Long organizerId, CreateFreeGameCommand command);

}
