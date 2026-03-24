package com.gumraze.rallyon.backend.user.application.port.in.command;

import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;

import java.util.UUID;

public record CreateMyProfileCommand(
        UUID userId,
        String nickname,
        UUID districtId,
        Grade regionalGrade,
        Grade nationalGrade,
        String birth,
        Gender gender
) {
}
