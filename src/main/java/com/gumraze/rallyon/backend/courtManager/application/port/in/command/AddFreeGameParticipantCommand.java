package com.gumraze.rallyon.backend.courtManager.application.port.in.command;

import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;

public record AddFreeGameParticipantCommand(
        Long id,
        String name,
        Gender gender,
        Grade grade,
        Integer age
) {
}
