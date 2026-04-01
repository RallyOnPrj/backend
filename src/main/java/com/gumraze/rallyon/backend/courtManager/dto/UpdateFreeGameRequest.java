package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateFreeGameRequest(
        String title,
        MatchRecordMode matchRecordMode,
        GradeType gradeType,
        String scheduledAt,
        @Size(max = 255) String location,
        List<UUID> managerIds
) {}
