package com.gumraze.rallyon.backend.api.courtManager;

import com.gumraze.rallyon.backend.api.common.ApiBearerAuth;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameRoundMatchResponse;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@ApiBearerAuth
public interface FreeGameQueryApi {

    ResponseEntity<FreeGameDetailResponse> getFreeGameDetail(
            UUID userId,
            UUID gameId
    );

    ResponseEntity<FreeGameRoundMatchResponse> getFreeGameRoundMatchResponse(
            UUID userId,
            UUID gameId
    );
}
