package com.gumraze.rallyon.backend.user.application.port.in.command;

import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;

import java.util.UUID;

public record UpdateMyProfileCommand(
        UUID accountId,
        String nickname,
        String tag,
        Grade regionalGrade,
        Grade nationalGrade,
        String birth,
        Boolean birthVisible,
        UUID districtId,
        String profileImageUrl,
        Gender gender
) {
}
