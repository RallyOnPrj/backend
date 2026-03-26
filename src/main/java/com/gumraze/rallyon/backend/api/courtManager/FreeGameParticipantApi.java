package com.gumraze.rallyon.backend.api.courtManager;

import com.gumraze.rallyon.backend.api.common.ApiBearerAuth;
import com.gumraze.rallyon.backend.courtManager.dto.AddFreeGameParticipantRequest;
import com.gumraze.rallyon.backend.courtManager.dto.AddFreeGameParticipantResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantsResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@ApiBearerAuth
public interface FreeGameParticipantApi {

    ResponseEntity<AddFreeGameParticipantResponse> addFreeGameParticipant(
            UUID accountId,
            UUID gameId,
            AddFreeGameParticipantRequest request
    );

    ResponseEntity<FreeGameParticipantsResponse> getFreeGameParticipants(
            UUID accountId,
            UUID gameId,
            String include
    );

    ResponseEntity<FreeGameParticipantDetailResponse> getFreeGameParticipantDetail(
            UUID accountId,
            UUID gameId,
            UUID participantId
    );
}
