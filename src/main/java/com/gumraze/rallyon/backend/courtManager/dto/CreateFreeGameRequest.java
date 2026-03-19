package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CreateFreeGameRequest(
        @NotBlank
        String title,

        MatchRecordMode matchRecordMode,

        @NotNull
        GradeType gradeType,

        @NotNull
        @Min(1)
        Integer courtCount,

        @NotNull
        @Min(1)
        Integer roundCount,

        @Size(max = 255)
        String location,

        @Size(max = 2)
        List<Long> managerIds,

        @Valid
        List<ParticipantRequest> participants,

        @Valid
        List<RoundRequest> rounds
) {
    public record ParticipantRequest(
            @NotBlank
            String clientId,

            Long userId,

            @NotBlank
            String originalName,

            @NotNull
            Gender gender,

            @NotNull
            Grade grade,

            @NotNull
            @Min(10)
            @Max(70)
            Integer ageGroup
    ) {
    }

    public record RoundRequest(
            @NotNull
            @Min(1)
            Integer roundNumber,

            @NotNull
            @Valid
            List<CourtRequest> courts
    ) {
    }

    public record CourtRequest(
            @NotNull
            @Min(1)
            Integer courtNumber,

            @NotNull
            @Size(min = 4, max = 4)
            List<String> slots
    ) {
    }
}
