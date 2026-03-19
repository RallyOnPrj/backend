package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.CourtAssignment;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.RoundAssignment;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.repository.FreeGameMatchRepository;
import com.gumraze.rallyon.backend.courtManager.repository.FreeGameRoundRepository;
import com.gumraze.rallyon.backend.courtManager.repository.GameParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SaveFreeGameRoundPersistenceAdapter implements SaveFreeGameRoundPort {

    private final FreeGameRoundRepository freeGameRoundRepository;
    private final FreeGameMatchRepository freeGameMatchRepository;
    private final GameParticipantRepository gameParticipantRepository;

    @Override
    public void saveAll(FreeGame freeGame, List<RoundAssignment> roundAssignments) {
        if (roundAssignments == null || roundAssignments.isEmpty()) {
            return;
        }

        for (RoundAssignment roundAssignment : roundAssignments) {
            validateRoundAssignment(roundAssignment);

            FreeGameRound savedRound = freeGameRoundRepository.save(
                    FreeGameRound.builder()
                            .freeGame(freeGame)
                            .roundNumber(roundAssignment.roundNumber())
                            .roundStatus(RoundStatus.NOT_STARTED)
                            .build()
            );

            for (CourtAssignment courtAssignment : roundAssignment.courts()) {
                GameParticipant slot1 = loadParticipant(courtAssignment.slot1ParticipantId());
                GameParticipant slot2 = loadParticipant(courtAssignment.slot2ParticipantId());
                GameParticipant slot3 = loadParticipant(courtAssignment.slot3ParticipantId());
                GameParticipant slot4 = loadParticipant(courtAssignment.slot4ParticipantId());

                freeGameMatchRepository.save(
                        FreeGameMatch.builder()
                                .round(savedRound)
                                .courtNumber(courtAssignment.courtNumber())
                                .teamAPlayer1(slot1)
                                .teamAPlayer2(slot3)
                                .teamBPlayer1(slot2)
                                .teamBPlayer2(slot4)
                                .matchStatus(MatchStatus.NOT_STARTED)
                                .matchResult(MatchResult.NULL)
                                .isActive(true)
                                .build()
                );
            }
        }
    }

    private void validateRoundAssignment(RoundAssignment roundAssignment) {
        Set<UUID> assignedParticipantsInRound = new HashSet<>();

        for (CourtAssignment courtAssignment : roundAssignment.courts()) {
            validateCourtAssignment(courtAssignment);

            for (UUID participantId : Arrays.asList(
                    courtAssignment.slot1ParticipantId(),
                    courtAssignment.slot2ParticipantId(),
                    courtAssignment.slot3ParticipantId(),
                    courtAssignment.slot4ParticipantId()
            )) {
                if (participantId == null) {
                    continue;
                }
                if (!assignedParticipantsInRound.add(participantId)) {
                    throw new IllegalArgumentException("같은 라운드에는 동일한 참가자를 중복 배정할 수 없습니다.");
                }
            }
        }
    }

    private void validateCourtAssignment(CourtAssignment courtAssignment) {
        Set<UUID> assignedParticipantsInCourt = new HashSet<>();

        for (UUID participantId : Arrays.asList(
                courtAssignment.slot1ParticipantId(),
                courtAssignment.slot2ParticipantId(),
                courtAssignment.slot3ParticipantId(),
                courtAssignment.slot4ParticipantId()
        )) {
            if (participantId == null) {
                continue;
            }
            if (!assignedParticipantsInCourt.add(participantId)) {
                throw new IllegalArgumentException("같은 코트에는 동일한 참가자를 중복 배정할 수 없습니다.");
            }
        }
    }

    private GameParticipant loadParticipant(UUID participantId) {
        if (participantId == null) {
            return null;
        }

        return gameParticipantRepository.findById(participantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 participantId입니다. participantId: " + participantId));
    }
}
