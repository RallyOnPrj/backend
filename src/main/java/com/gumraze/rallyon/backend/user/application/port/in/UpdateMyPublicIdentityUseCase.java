package com.gumraze.rallyon.backend.user.application.port.in;

import com.gumraze.rallyon.backend.user.application.port.in.command.UpdateMyPublicIdentityCommand;

public interface UpdateMyPublicIdentityUseCase {

    void update(UpdateMyPublicIdentityCommand command);
}
