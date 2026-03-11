package com.gumraze.drive.drive_backend.courtManager.controller;

import com.gumraze.drive.drive_backend.api.courtManager.CourtManagerApi;
import com.gumraze.drive.drive_backend.courtManager.dto.*;
import com.gumraze.drive.drive_backend.courtManager.service.FreeGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/free-games")
@RequiredArgsConstructor
public class CourtManagerController implements CourtManagerApi {

    private final FreeGameService freeGameService;

    @Override
    @PostMapping()
    public ResponseEntity<CreateFreeGameResponse> createFreeGame(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CreateFreeGameRequest request
    ) {

        // 서비스 호출
        CreateFreeGameResponse response = freeGameService.createFreeGame(userId, request);
        URI location = URI.create("/free-games/" + response.getGameId());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @Override
    @GetMapping("/{gameId}")
    public ResponseEntity<FreeGameDetailResponse> getFreeGameDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId
    ) {
        FreeGameDetailResponse response = freeGameService.getFreeGameDetail(userId, gameId);

        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/{gameId}")
    public ResponseEntity<UpdateFreeGameResponse> updateFreeGameInfo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId,
            @RequestBody @Valid UpdateFreeGameRequest request
    ) {
        UpdateFreeGameResponse response = freeGameService.updateFreeGameInfo(userId, gameId, request);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{gameId}/rounds-and-matches")
    public ResponseEntity<FreeGameRoundMatchResponse> getFreeGameRoundMatchResponse(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId
    ) {
        FreeGameRoundMatchResponse response = freeGameService.getFreeGameRoundMatchResponse(userId, gameId);

        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/{gameId}/rounds-and-matches")
    public ResponseEntity<Void> updateFreeGameRoundMatch(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId,
            @RequestBody @Valid UpdateFreeGameRoundMatchRequest request
    ) {
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{gameId}/participants")
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

    @Override
    @GetMapping("/{gameId}/participants/{participantId}")
    public ResponseEntity<FreeGameParticipantDetailResponse> getFreeGameParticipantDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId,
            @PathVariable Long participantId
    ) {
        FreeGameParticipantDetailResponse response =
                freeGameService.getFreeGameParticipantDetail(userId, gameId, participantId);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/share/{shareCode}")
    public ResponseEntity<FreeGameDetailResponse> getPublicFreeGameDetail(
            @PathVariable String shareCode
    ) {
        FreeGameDetailResponse response = freeGameService.getPublicFreeGameDetail(shareCode);
        return ResponseEntity.ok(response);
    }

}
