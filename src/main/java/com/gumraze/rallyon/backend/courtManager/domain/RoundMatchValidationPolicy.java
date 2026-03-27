package com.gumraze.rallyon.backend.courtManager.domain;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameRoundsAndMatchesCommand;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class RoundMatchValidationPolicy {

    private RoundMatchValidationPolicy() {
    }

    public static void validate(
            List<UpdateFreeGameRoundsAndMatchesCommand.Round> rounds,
            Set<UUID> participantIdsInGame
    ) {
        if (rounds == null) {
            return;
        }

        for (UpdateFreeGameRoundsAndMatchesCommand.Round round : rounds) {
            Integer requestedRoundNumber = round.roundNumber();
            if (requestedRoundNumber == null) {
                throw new IllegalArgumentException("roundNumber는 필수입니다.");
            }

            if (round.matches() == null || round.matches().isEmpty()) {
                throw new IllegalArgumentException("라운드는 최소 1개의 매치를 포함해야합니다.");
            }

            Set<UUID> usedParticipantIds = new HashSet<>();
            List<Integer> courtNumbers = round.matches().stream()
                    .map(UpdateFreeGameRoundsAndMatchesCommand.Match::courtNumber)
                    .toList();

            for (UpdateFreeGameRoundsAndMatchesCommand.Match match : round.matches()) {
                validateMatchParticipants(match.teamAIds(), match.teamBIds(), participantIdsInGame, usedParticipantIds);
            }

            if (courtNumbers.stream().anyMatch(n -> n == null || n < 1)) {
                throw new IllegalArgumentException("courtNumber는 1이상이어야 합니다.");
            }

            List<Integer> sortedCourtNumbers = courtNumbers.stream().sorted().toList();
            for (int i = 0; i < sortedCourtNumbers.size(); i++) {
                if (sortedCourtNumbers.get(i) != i + 1) {
                    throw new IllegalArgumentException("courtNumber는 1..n 연속이어야 합니다.");
                }
            }

            if (courtNumbers.stream().distinct().count() != courtNumbers.size()) {
                throw new IllegalArgumentException("매치는 서로 다른 courtNumber를 가져야합니다.");
            }
        }
    }

    private static void validateMatchParticipants(
            List<UUID> teamAIds,
            List<UUID> teamBIds,
            Set<UUID> participantIdsInGame,
            Set<UUID> usedParticipantIds
    ) {
        if (teamAIds == null || teamBIds == null) {
            throw new IllegalArgumentException("teamAIds와 teamBIds는 모두 필수입니다.");
        }

        if (teamAIds.size() != 2 || teamBIds.size() != 2) {
            throw new IllegalArgumentException("teamAIds와 teamBIds의 길이는 2여야 합니다.");
        }

        Set<UUID> matchParticipantIds = new HashSet<>();
        validateTeamIds(teamAIds, participantIdsInGame, usedParticipantIds, matchParticipantIds);
        validateTeamIds(teamBIds, participantIdsInGame, usedParticipantIds, matchParticipantIds);
    }

    private static void validateTeamIds(
            List<UUID> teamIds,
            Set<UUID> participantIdsInGame,
            Set<UUID> usedParticipantIds,
            Set<UUID> matchParticipantIds
    ) {
        for (UUID participantId : teamIds) {
            if (participantId == null) {
                continue;
            }

            if (!participantIdsInGame.contains(participantId)) {
                throw new IllegalArgumentException(
                        "존재하지 않거나 해당 게임에 속하지 않는 participantId입니다. participantId: " + participantId
                );
            }

            if (!matchParticipantIds.add(participantId)) {
                throw new IllegalArgumentException("match 내 participantId 중복입니다. participantId: " + participantId);
            }

            if (!usedParticipantIds.add(participantId)) {
                throw new IllegalArgumentException("round 내 participantId 중복입니다. participantId: " + participantId);
            }
        }
    }
}
