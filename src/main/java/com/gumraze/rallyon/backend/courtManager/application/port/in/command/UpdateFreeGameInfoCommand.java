package com.gumraze.rallyon.backend.courtManager.application.port.in.command;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.user.constants.GradeType;

import java.util.List;
import java.util.UUID;

public record UpdateFreeGameInfoCommand(
        UUID organizerId,
        UUID gameId,
        String title,
        MatchRecordMode matchRecordMode,
        GradeType gradeType,
        String scheduledAt,
        String location,
        List<UUID> managerIds
) {
}
