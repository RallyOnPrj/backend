package com.gumraze.rallyon.backend.identity.application.port.in;

import com.gumraze.rallyon.backend.identity.application.port.in.command.RegisterLocalIdentityCommand;
import java.util.UUID;

public interface RegisterLocalIdentityUseCase {

    UUID register(RegisterLocalIdentityCommand command);
}
