package com.gumraze.rallyon.backend.user.application.port.in;

import com.gumraze.rallyon.backend.user.application.port.in.command.CreateMyProfileCommand;

public interface CreateMyProfileUseCase {

    void create(CreateMyProfileCommand command);
}
