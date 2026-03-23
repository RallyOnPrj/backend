package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameRoundsAndMatchesUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameRoundsAndMatchesQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.support.FreeGameAccessSupport;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameMatchResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameRoundMatchResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameRoundResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFreeGameRoundsAndMatchesService implements GetFreeGameRoundsAndMatchesUseCase {

    private final LoadFreeGamePort loadFreeGamePort;
    private final LoadFreeGameRoundPort loadFreeGameRoundPort;
    private final LoadFreeGameMatchPort loadFreeGameMatchPort;

    @Override
    public FreeGameRoundMatchResponse get(GetFreeGameRoundsAndMatchesQuery query) {
        FreeGameAccessSupport.loadOrganizerGame(loadFreeGamePort, query.organizerId(), query.gameId());

        List<FreeGameRound> rounds = loadFreeGameRoundPort.loadRoundsByGameIdOrderByRoundNumber(query.gameId());
        if (rounds.isEmpty()) {
            return FreeGameRoundMatchResponse.builder()
                    .gameId(query.gameId())
                    .rounds(List.of())
                    .build();
        }

        List<UUID> roundIds = rounds.stream()
                .map(FreeGameRound::getId)
                .toList();
        List<FreeGameMatch> matches = loadFreeGameMatchPort.loadMatchesByRoundIdsOrderByCourtNumber(roundIds);
        Map<UUID, List<FreeGameMatch>> matchesByRoundId = matches.stream()
                .collect(Collectors.groupingBy(match -> match.getRound().getId()));

        List<FreeGameRoundResponse> roundResponses = rounds.stream()
                .map(round -> FreeGameRoundResponse.builder()
                        .roundNumber(round.getRoundNumber())
                        .roundStatus(round.getRoundStatus())
                        .matches(matchesByRoundId.getOrDefault(round.getId(), List.of()).stream()
                                .map(this::toMatchResponse)
                                .toList())
                        .build())
                .toList();

        return FreeGameRoundMatchResponse.builder()
                .gameId(query.gameId())
                .rounds(roundResponses)
                .build();
    }

    private FreeGameMatchResponse toMatchResponse(FreeGameMatch match) {
        return FreeGameMatchResponse.builder()
                .courtNumber(match.getCourtNumber().longValue())
                .teamAIds(Arrays.asList(
                        match.getTeamAPlayer1() != null ? match.getTeamAPlayer1().getId() : null,
                        match.getTeamAPlayer2() != null ? match.getTeamAPlayer2().getId() : null
                ))
                .teamBIds(Arrays.asList(
                        match.getTeamBPlayer1() != null ? match.getTeamBPlayer1().getId() : null,
                        match.getTeamBPlayer2() != null ? match.getTeamBPlayer2().getId() : null
                ))
                .matchStatus(match.getMatchStatus())
                .matchResult(match.getMatchResult() != null ? match.getMatchResult() : MatchResult.NULL)
                .isActive(match.getIsActive())
                .build();
    }
}
