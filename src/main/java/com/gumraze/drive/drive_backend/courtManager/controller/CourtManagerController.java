package com.gumraze.drive.drive_backend.courtManager.controller;

import com.gumraze.drive.drive_backend.api.courtManager.CourtManagerApi;
import com.gumraze.drive.drive_backend.courtManager.dto.*;
import com.gumraze.drive.drive_backend.courtManager.service.FreeGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CourtManagerController implements CourtManagerApi {

    private final FreeGameService freeGameService;

    @Override
    @PostMapping("/free-games")
    public ResponseEntity<CreateFreeGameResponse> createFreeGame(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CreateFreeGameRequest request
    ) {

        // 서비스 호출
        CreateFreeGameResponse response = freeGameService.createFreeGame(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Override
    @GetMapping("/free-games/{gameId}")
    public ResponseEntity<FreeGameDetailResponse> getFreeGameDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId
    ) {
        FreeGameDetailResponse response = freeGameService.getFreeGameDetail(userId, gameId);

        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/free-games/{gameId}")
    public ResponseEntity<UpdateFreeGameResponse> updateFreeGameInfo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId,
            @RequestBody @Valid UpdateFreeGameRequest request
    ) {
        UpdateFreeGameResponse response = freeGameService.updateFreeGameInfo(userId, gameId, request);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/free-games/{gameId}/rounds-and-matches")
    public ResponseEntity<FreeGameRoundMatchResponse> getFreeGameRoundMatchResponse(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId
    ) {
        FreeGameRoundMatchResponse response = freeGameService.getFreeGameRoundMatchResponse(userId, gameId);

        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/free-games/{gameId}/rounds-and-matches")
    public ResponseEntity<Void> updateFreeGameRoundMatch(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId,
            @RequestBody @Valid UpdateFreeGameRoundMatchRequest request
    ) {
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/free-games/{gameId}/participants")
    public ResponseEntity<FreeGameParticipantsResponse> getFreeGameParticipants(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId,
            @RequestParam(name = "include", required = false) String include
    ) {
        boolean includeStats = "stats".equalsIgnoreCase(include);
        FreeGameParticipantsResponse response =
                freeGameService.getFreeGameParticipants(userId, gameId, includeStats);

        return ResponseEntity.ok(response);
    }
}
