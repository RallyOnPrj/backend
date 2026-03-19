package com.gumraze.rallyon.backend.courtManager.controller;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreateFreeGameCommandMapper {

    public CreateFreeGameCommand toCommand(CreateFreeGameRequest request) {
        return new CreateFreeGameCommand(
                request.title(),
                request.matchRecordMode(),
                request.gradeType(),
                request.courtCount(),
                request.roundCount(),
                request.location(),
                request.managerIds(),
                mapParticipants(request.participants()),
                mapRounds(request.rounds())
        );
    }

    private List<CreateFreeGameCommand.Participant> mapParticipants(
            List<CreateFreeGameRequest.ParticipantRequest> participants
    ) {
        if (participants == null) {
            return List.of();
        }

        return participants.stream()
                .map(participant -> new CreateFreeGameCommand.Participant(
                        participant.clientId(),
                        participant.userId(),
                        participant.originalName(),
                        participant.gender(),
                        participant.grade(),
                        participant.ageGroup()
                ))
                .toList();
    }

    private List<CreateFreeGameCommand.Round> mapRounds(
            List<CreateFreeGameRequest.RoundRequest> rounds
    ) {
        if (rounds == null) {
            return List.of();
        }

        return rounds.stream()
                .map(round -> new CreateFreeGameCommand.Round(
                        round.roundNumber(),
                        mapCourts(round.courts())
                ))
                .toList();
    }

    private List<CreateFreeGameCommand.Court> mapCourts(
            List<CreateFreeGameRequest.CourtRequest> courts
    ) {
        if (courts == null) {
            return List.of();
        }

        return courts.stream()
                .map(court -> new CreateFreeGameCommand.Court(
                        court.courtNumber(),
                        court.slots()
                ))
                .toList();
    }
}
