package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.AddFreeGameParticipantUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.CreateFreeGameUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameDetailUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameParticipantDetailUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameParticipantsUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameRoundsAndMatchesUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetPublicFreeGameDetailUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.UpdateFreeGameInfoUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.UpdateFreeGameRoundsAndMatchesUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameInfoCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameRoundsAndMatchesCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameDetailQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameParticipantDetailQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameParticipantsQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameRoundsAndMatchesQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetPublicFreeGameDetailQuery;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.dto.AddFreeGameParticipantRequest;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameMatchResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantsResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameRoundMatchResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameRoundResponse;
import com.gumraze.rallyon.backend.courtManager.dto.MatchRequest;
import com.gumraze.rallyon.backend.courtManager.dto.RoundRequest;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRequest;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRoundMatchRequest;
import com.gumraze.rallyon.backend.security.config.SecurityConfig;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.authenticatedUser;
import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.freeGameDetailResponse;
import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.participantDetailResponse;
import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.participantResponse;
import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.participantResponseWithStats;
import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.participantsResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
class CourtManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private CreateFreeGameUseCase createFreeGameUseCase;
    @MockitoBean private GetFreeGameDetailUseCase getFreeGameDetailUseCase;
    @MockitoBean private UpdateFreeGameInfoUseCase updateFreeGameInfoUseCase;
    @MockitoBean private GetFreeGameRoundsAndMatchesUseCase getFreeGameRoundsAndMatchesUseCase;
    @MockitoBean private UpdateFreeGameRoundsAndMatchesUseCase updateFreeGameRoundsAndMatchesUseCase;
    @MockitoBean private AddFreeGameParticipantUseCase addFreeGameParticipantUseCase;
    @MockitoBean private GetFreeGameParticipantsUseCase getFreeGameParticipantsUseCase;
    @MockitoBean private GetFreeGameParticipantDetailUseCase getFreeGameParticipantDetailUseCase;
    @MockitoBean private GetPublicFreeGameDetailUseCase getPublicFreeGameDetailUseCase;

    @MockitoBean private CreateFreeGameCommandMapper createFreeGameCommandMapper;
    @MockitoBean private UpdateFreeGameInfoCommandMapper updateFreeGameInfoCommandMapper;
    @MockitoBean private UpdateFreeGameRoundsAndMatchesCommandMapper updateFreeGameRoundsAndMatchesCommandMapper;
    @MockitoBean private AddFreeGameParticipantCommandMapper addFreeGameParticipantCommandMapper;
    @MockitoBean private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("자유게임 생성 요청을 create use case로 전달한다")
    void createFreeGame_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        CreateFreeGameRequest request = new CreateFreeGameRequest(
                "자유게임1",
                null,
                GradeType.NATIONAL,
                2,
                3,
                "잠실 배드민턴장",
                null,
                List.of(),
                List.of()
        );
        CreateFreeGameCommand command = new CreateFreeGameCommand(
                "자유게임1",
                null,
                GradeType.NATIONAL,
                2,
                3,
                "잠실 배드민턴장",
                null,
                List.of(),
                List.of()
        );

        given(createFreeGameCommandMapper.toCommand(request)).willReturn(command);
        given(createFreeGameUseCase.create(userId, command)).willReturn(gameId);

        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(userId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/free-games/" + gameId))
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));

        verify(createFreeGameCommandMapper).toCommand(request);
        verify(createFreeGameUseCase).create(userId, command);
    }

    @Test
    @DisplayName("자유게임 상세 조회 성공")
    void getFreeGameDetail_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        FreeGameDetailResponse response = freeGameDetailResponse(userId, gameId);

        when(getFreeGameDetailUseCase.get(new GetFreeGameDetailQuery(userId, gameId))).thenReturn(response);

        mockMvc.perform(get("/free-games/{gameId}", gameId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.title").value("자유게임"));
    }

    @Test
    @DisplayName("자유게임 기본 정보 수정 성공")
    void updateFreeGameInfo_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UpdateFreeGameRequest request = UpdateFreeGameRequest.builder()
                .title("수정된 자유게임")
                .matchRecordMode(MatchRecordMode.RESULT)
                .gradeType(GradeType.REGIONAL)
                .build();
        UpdateFreeGameInfoCommand command = new UpdateFreeGameInfoCommand(
                userId,
                gameId,
                request.getTitle(),
                request.getMatchRecordMode(),
                request.getGradeType(),
                request.getLocation(),
                request.getManagerIds()
        );
        UpdateFreeGameResponse response = UpdateFreeGameResponse.builder().gameId(gameId).build();

        when(updateFreeGameInfoCommandMapper.toCommand(eq(userId), eq(gameId), any(UpdateFreeGameRequest.class)))
                .thenReturn(command);
        when(updateFreeGameInfoUseCase.update(command)).thenReturn(response);

        mockMvc.perform(patch("/free-games/{gameId}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(userId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    @DisplayName("자유게임 라운드/매치 조회 성공")
    void getFreeGameRoundMatch_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID teamA1 = UUID.randomUUID();
        UUID teamA2 = UUID.randomUUID();
        UUID teamB1 = UUID.randomUUID();
        UUID teamB2 = UUID.randomUUID();

        FreeGameRoundMatchResponse response = FreeGameRoundMatchResponse.builder()
                .gameId(gameId)
                .rounds(List.of(
                        FreeGameRoundResponse.builder()
                                .roundNumber(1)
                                .roundStatus(RoundStatus.NOT_STARTED)
                                .matches(List.of(
                                        FreeGameMatchResponse.builder()
                                                .courtNumber(1L)
                                                .teamAIds(List.of(teamA1, teamA2))
                                                .teamBIds(List.of(teamB1, teamB2))
                                                .matchStatus(MatchStatus.NOT_STARTED)
                                                .matchResult(MatchResult.NULL)
                                                .isActive(true)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        when(getFreeGameRoundsAndMatchesUseCase.get(new GetFreeGameRoundsAndMatchesQuery(userId, gameId)))
                .thenReturn(response);

        mockMvc.perform(get("/free-games/{gameId}/rounds-and-matches", gameId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.rounds[0].matches[0].courtNumber").value(1L))
                .andExpect(jsonPath("$.rounds[0].matches[0].teamAIds[0]").value(teamA1.toString()));
    }

    @Test
    @DisplayName("라운드/매치 수정 PATCH 성공")
    void updateRoundsAndMatches_patch_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID teamA1 = UUID.randomUUID();
        UUID teamA2 = UUID.randomUUID();
        UUID teamB1 = UUID.randomUUID();
        UUID teamB2 = UUID.randomUUID();

        UpdateFreeGameRoundMatchRequest request = UpdateFreeGameRoundMatchRequest.builder()
                .rounds(List.of(
                        RoundRequest.builder()
                                .roundNumber(1)
                                .matches(List.of(
                                        MatchRequest.builder()
                                                .courtNumber(1)
                                                .teamAIds(List.of(teamA1, teamA2))
                                                .teamBIds(List.of(teamB1, teamB2))
                                                .build()
                                ))
                                .build()
                ))
                .build();
        UpdateFreeGameRoundsAndMatchesCommand command = new UpdateFreeGameRoundsAndMatchesCommand(
                userId,
                gameId,
                List.of(new UpdateFreeGameRoundsAndMatchesCommand.Round(
                        1,
                        List.of(new UpdateFreeGameRoundsAndMatchesCommand.Match(
                                1,
                                List.of(teamA1, teamA2),
                                List.of(teamB1, teamB2)
                        ))
                ))
        );

        when(updateFreeGameRoundsAndMatchesCommandMapper.toCommand(
                eq(userId),
                eq(gameId),
                any(UpdateFreeGameRoundMatchRequest.class)
        )).thenReturn(command);

        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(userId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(updateFreeGameRoundsAndMatchesUseCase).update(command);
    }

    @Test
    @DisplayName("참가자 추가 성공")
    void addFreeGameParticipant_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        AddFreeGameParticipantRequest request = new AddFreeGameParticipantRequest(
                null,
                "참가자",
                Gender.MALE,
                Grade.ROOKIE,
                20
        );
        AddFreeGameParticipantCommand command = new AddFreeGameParticipantCommand(
                null,
                "참가자",
                Gender.MALE,
                Grade.ROOKIE,
                20
        );

        when(addFreeGameParticipantCommandMapper.toCommand(request)).thenReturn(command);
        when(addFreeGameParticipantUseCase.add(userId, gameId, command)).thenReturn(participantId);

        mockMvc.perform(post("/free-games/{gameId}/participants", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(userId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/free-games/" + gameId + "/participants/" + participantId))
                .andExpect(jsonPath("$.participantId").value(participantId.toString()));
    }

    @Test
    @DisplayName("자유게임 참가자 목록 조회 성공")
    void get_free_game_participants_with_stats_success() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID participantUserId = UUID.randomUUID();
        FreeGameParticipantResponse participant = participantResponse(UUID.randomUUID(), participantUserId, "KimA");
        FreeGameParticipantsResponse response =
                participantsResponse(gameId, List.of(participantResponseWithStats(participant, 3, 2, 1, 1)));

        when(getFreeGameParticipantsUseCase.get(new GetFreeGameParticipantsQuery(userId, gameId, true)))
                .thenReturn(response);

        mockMvc.perform(get("/free-games/{gameId}/participants", gameId)
                        .queryParam("include", "stats")
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants[0].assignedMatchCount").value(3));
    }

    @Test
    @DisplayName("자유게임 참가자 상세 조회 성공")
    void get_free_game_participant_detail_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        UUID participantUserId = UUID.randomUUID();
        FreeGameParticipantDetailResponse response =
                participantDetailResponse(gameId, participantId, participantUserId, "KimA");

        when(getFreeGameParticipantDetailUseCase.get(new GetFreeGameParticipantDetailQuery(userId, gameId, participantId)))
                .thenReturn(response);

        mockMvc.perform(get("/free-games/{gameId}/participants/{participantId}", gameId, participantId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantId").value(participantId.toString()));
    }

    @Test
    @DisplayName("공개 자유게임 상세 조회 성공")
    void get_public_free_game_detail_success() throws Exception {
        String shareCode = "public-share-code";
        UUID organizerId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        FreeGameDetailResponse response = freeGameDetailResponse(organizerId, gameId);

        when(getPublicFreeGameDetailUseCase.get(new GetPublicFreeGameDetailQuery(shareCode))).thenReturn(response);

        mockMvc.perform(get("/free-games/share/{shareCode}", shareCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    @DisplayName("참가자 상세 조회 시 존재하지 않는 participantId면 실패")
    void get_free_game_participant_detail_with_unknown_participant_then_not_found() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        when(getFreeGameParticipantDetailUseCase.get(new GetFreeGameParticipantDetailQuery(userId, gameId, participantId)))
                .thenThrow(new NotFoundException("존재하지 않는 참가자입니다. participantId: " + participantId));

        mockMvc.perform(get("/free-games/{gameId}/participants/{participantId}", gameId, participantId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("존재하지 않는 참가자입니다. participantId: " + participantId));
    }
}
