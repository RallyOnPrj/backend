package com.gumraze.rallyon.backend.courtManager.domain;

import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;

import java.util.Collection;
import java.util.Objects;

public final class ParticipantDisplayNamePolicy {

    private ParticipantDisplayNamePolicy() {
    }

    public static String resolve(
            String originalName,
            Gender gender,
            Grade grade,
            Integer ageGroup,
            Collection<GameParticipant> existingParticipants
    ) {
        int duplicateCount = (int) existingParticipants.stream()
                .filter(participant -> Objects.equals(participant.getOriginalName(), originalName))
                .filter(participant -> participant.getGender() == gender)
                .filter(participant -> participant.getGrade() == grade)
                .filter(participant -> Objects.equals(participant.getAgeGroup(), ageGroup))
                .count();

        return duplicateCount == 0
                ? originalName
                : originalName + suffix(duplicateCount);
    }

    private static String suffix(int count) {
        return String.valueOf((char) ('A' + count - 1));
    }
}
