package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.CreateFreeGameUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadUserPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.CourtAssignment;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.RoundAssignment;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateFreeGameService implements CreateFreeGameUseCase {

    private final LoadUserPort loadUserPort;
    private final SaveFreeGamePort saveFreeGamePort;
    private final SaveGameParticipantPort saveGameParticipantPort;
    private final SaveFreeGameRoundPort saveFreeGameRoundPort;

    @Override
    public UUID create(Long organizerId, CreateFreeGameCommand command) {
        User organizer = loadUserPort.loadById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 organizer 입니다."));

        MatchRecordMode matchRecordMode = command.matchRecordMode() == null
                ? MatchRecordMode.STATUS_ONLY
                : command.matchRecordMode();

        FreeGame freeGame = FreeGame.builder()
                .title(command.title())
                .organizer(organizer)
                .gradeType(command.gradeType())
                .matchRecordMode(matchRecordMode)
                .location(command.location())
                .build();

        FreeGame savedGame = saveFreeGamePort.save(freeGame);

        Map<String, GameParticipant> participantsByClientId =
                saveGameParticipantPort.saveAll(savedGame, command.participants());

        List<RoundAssignment> roundAssignments = toRoundAssignments(
                command.rounds(),
                participantsByClientId
        );

        saveFreeGameRoundPort.saveAll(savedGame, roundAssignments);

        return savedGame.getId();
    }

    private List<RoundAssignment> toRoundAssignments(
            List<CreateFreeGameCommand.Round> rounds,
            Map<String, GameParticipant> participantsByClientId
    ) {
        if (rounds == null || rounds.isEmpty()) {
            return List.of();
        }

        return rounds.stream()
                .map(round -> new RoundAssignment(
                        round.roundNumber(),
                        round.courts().stream()
                                .map(court -> new CourtAssignment(
                                        court.courtNumber(),
                                        resolveParticipantId(participantsByClientId, court.slots().get(0)),
                                        resolveParticipantId(participantsByClientId, court.slots().get(1)),
                                        resolveParticipantId(participantsByClientId, court.slots().get(2)),
                                        resolveParticipantId(participantsByClientId, court.slots().get(3))
                                ))
                                .toList()
                ))
                .toList();
    }

    private UUID resolveParticipantId(
            Map<String, GameParticipant> participantsByClientId,
            String clientId
    ) {
        GameParticipant participant = participantsByClientId.get(clientId);
        if (participant == null) {
            throw new IllegalArgumentException("참가자 매핑에 실패했습니다. clientId: " + clientId);
        }
        return participant.getId();
    }
}
