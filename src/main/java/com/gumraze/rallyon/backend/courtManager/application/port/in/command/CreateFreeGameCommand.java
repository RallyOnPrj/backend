package com.gumraze.rallyon.backend.courtManager.application.port.in.command;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import java.util.List;

public record CreateFreeGameCommand (
    String title,
    MatchRecordMode matchRecordMode,
    GradeType gradeType,
    Integer courtCount,
    Integer roundCount,
    String location,
    List<Long> managerIds,
    List<Participant> participants,
    List<Round> rounds
) {
    public record Participant (
        String clientId,
        Long userId,
        String originalName,
        Gender gender,
        Grade grade,
        Integer ageGroup
    ) { }

    public record Round (
        Integer roundNumber,
        List<Court> courts
    ) { }

    public record Court (
        Integer courtNumber,
        List<String> slots
    ) { }
}
