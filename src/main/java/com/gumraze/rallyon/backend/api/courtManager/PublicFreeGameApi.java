package com.gumraze.rallyon.backend.api.courtManager;

import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import org.springframework.http.ResponseEntity;

public interface PublicFreeGameApi {

    ResponseEntity<FreeGameDetailResponse> getPublicFreeGameDetail(String shareCode);
}
