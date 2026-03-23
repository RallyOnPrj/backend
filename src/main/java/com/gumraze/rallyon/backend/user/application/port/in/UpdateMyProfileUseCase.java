package com.gumraze.rallyon.backend.user.application.port.in;

import com.gumraze.rallyon.backend.user.application.port.in.command.UpdateMyProfileCommand;

public interface UpdateMyProfileUseCase {

    void update(UpdateMyProfileCommand command);
}
