package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameParticipantDetailUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameParticipantDetailQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.application.support.FreeGameAccessSupport;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantDetailResponse;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFreeGameParticipantDetailService implements GetFreeGameParticipantDetailUseCase {

    private final LoadFreeGamePort loadFreeGamePort;
    private final LoadGameParticipantPort loadGameParticipantPort;

    @Override
    public FreeGameParticipantDetailResponse get(GetFreeGameParticipantDetailQuery query) {
        FreeGameAccessSupport.loadOrganizerGame(loadFreeGamePort, query.organizerId(), query.gameId());

        GameParticipant participant = loadGameParticipantPort.loadParticipantById(query.participantId())
                .orElseThrow(() ->
                        new NotFoundException("존재하지 않는 참가자입니다. participantId: " + query.participantId()));

        if (!participant.getFreeGame().getId().equals(query.gameId())) {
            throw new NotFoundException("참가자가 다른 게임에 속해 있습니다. participantId: " + query.participantId());
        }

        return FreeGameParticipantDetailResponse.builder()
                .gameId(query.gameId())
                .participantId(participant.getId())
                .userId(participant.getUser() != null ? participant.getUser().getId() : null)
                .displayName(participant.getDisplayName())
                .gender(participant.getGender())
                .grade(participant.getGrade())
                .ageGroup(participant.getAgeGroup())
                .build();
    }
}
