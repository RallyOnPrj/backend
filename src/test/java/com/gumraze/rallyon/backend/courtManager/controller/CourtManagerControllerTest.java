package com.gumraze.rallyon.backend.courtManager.controller;

import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.config.SecurityConfig;
import com.gumraze.rallyon.backend.courtManager.application.port.in.CreateFreeGameUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.dto.*;
import com.gumraze.rallyon.backend.courtManager.service.FreeGameService;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
class CourtManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FreeGameService freeGameService;

    @MockitoBean
    private CreateFreeGameUseCase createFreeGameUseCase;

    @MockitoBean
    private CreateFreeGameCommandMapper createFreeGameCommandMapper;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @Test()
    @DisplayName("최소 필수값으로 자유게임 생성 성공")
    void createFreeGame_success() throws Exception {
        // given
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

        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(userId))
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/free-games/" + gameId))
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    @DisplayName("자유게임 생성 시, participant가 존재할 때 최소 필수 항목이 존재하면 성공 테스트")
    void createFreeGame_with_participant() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        CreateFreeGameRequest request = new CreateFreeGameRequest(
                "자유게임 1",
                null,
                GradeType.NATIONAL,
                2,
                3,
                "잠실 배드민턴장",
                null,
                List.of(
                        new CreateFreeGameRequest.ParticipantRequest(
                                "p1",
                                null,
                                "참가자 1",
                                Gender.MALE,
                                Grade.ROOKIE,
                                20
                        )
                ),
                List.of()
        );
        CreateFreeGameCommand command = new CreateFreeGameCommand(
                "자유게임 1",
                null,
                GradeType.NATIONAL,
                2,
                3,
                "잠실 배드민턴장",
                null,
                List.of(
                        new CreateFreeGameCommand.Participant(
                                "p1",
                                null,
                                "참가자 1",
                                Gender.MALE,
                                Grade.ROOKIE,
                                20
                        )
                ),
                List.of()
        );
        given(createFreeGameCommandMapper.toCommand(request)).willReturn(command);
        given(createFreeGameUseCase.create(userId, command)).willReturn(gameId);

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(userId))
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    @DisplayName("자유게임 상세 조회 성공 테스트")
    void getFreeGameDetail_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        FreeGameDetailResponse response = freeGameDetailResponse(userId, gameId);

        when(freeGameService.getFreeGameDetail(userId, gameId)).thenReturn(response);

        mockMvc.perform(get("/free-games/{gameId}", gameId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.title").value("자유게임"))
                .andExpect(jsonPath("$.gameType").value("FREE"))
                .andExpect(jsonPath("$.gameStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.matchRecordMode").value("STATUS_ONLY"))
                .andExpect(jsonPath("$.gradeType").value("NATIONAL"))
                .andExpect(jsonPath("$.location").value("잠실 배드민턴장"))
                .andExpect(jsonPath("$.courtCount").value(2))
                .andExpect(jsonPath("$.roundCount").value(2))
                .andExpect(jsonPath("$.organizerId").value(userId.toString()))
        ;
    }

    @Test
    @DisplayName("자유게임 상세 조회 시 존재하지 않는 gameId면 실패")
    void getFreeGameDetail_withUnknownGameId_returnError() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        when(freeGameService.getFreeGameDetail(userId, gameId))
                .thenThrow(new NotFoundException("게임이 존재하지 않습니다."));

        mockMvc.perform(get("/free-games/{gameId}", gameId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andDo(print()) // 응답 로그 출력
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").value("게임이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("자유게임 기본 정보 수정 성공 테스트")
    void updateFreeGameInfo_success() throws Exception {
        // given: 정상적인 요청 입력
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        UpdateFreeGameRequest request = UpdateFreeGameRequest.builder()
                .title("수정된 자유게임")
                .matchRecordMode(MatchRecordMode.RESULT)
                .gradeType(GradeType.REGIONAL)
                .build();
        String body = objectMapper.writeValueAsString(request);
        UpdateFreeGameResponse response = UpdateFreeGameResponse.builder()
                .gameId(gameId)
                .build();

        // stub
        when(freeGameService.updateFreeGameInfo(any(UUID.class), eq(gameId), any(UpdateFreeGameRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/free-games/{gameId}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(userId))
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
        ;
    }


    @Test
    @DisplayName("자유게임 라운드/매치 조회 성공 테스트")
    void getFreeGameRoundMatch_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID teamA1 = UUID.randomUUID();
        UUID teamA2 = UUID.randomUUID();
        UUID teamB1 = UUID.randomUUID();
        UUID teamB2 = UUID.randomUUID();

        FreeGameMatchResponse match =
                FreeGameMatchResponse.builder()
                        .courtNumber(1L)
                        .teamAIds(List.of(teamA1, teamA2))
                        .teamBIds(List.of(teamB1, teamB2))
                        .matchStatus(MatchStatus.NOT_STARTED)
                        .matchResult(MatchResult.NULL)
                        .isActive(true)
                        .build();

        FreeGameRoundResponse round =
                FreeGameRoundResponse.builder()
                        .roundNumber(1)
                        .roundStatus(RoundStatus.NOT_STARTED)
                        .matches(List.of(match))
                        .build();

        FreeGameRoundMatchResponse response =
                FreeGameRoundMatchResponse.builder()
                        .gameId(gameId)
                        .rounds(List.of(round))
                        .build();

        when(freeGameService.getFreeGameRoundMatchResponse(userId, gameId))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/rounds-and-matches", gameId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.rounds[0].roundNumber").value(1L))
                .andExpect(jsonPath("$.rounds[0].matches[0].courtNumber").value(1L))
                .andExpect(jsonPath("$.rounds[0].matches[0].teamAIds[0]").value(teamA1.toString()))
                .andExpect(jsonPath("$.rounds[0].matches[0].teamAIds[1]").value(teamA2.toString()))
                .andExpect(jsonPath("$.rounds[0].matches[0].teamBIds[0]").value(teamB1.toString()))
                .andExpect(jsonPath("$.rounds[0].matches[0].teamBIds[1]").value(teamB2.toString()))
                .andExpect(jsonPath("$.rounds[0].matches[0].matchStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.rounds[0].matches[0].matchResult").value("NULL"))
                .andExpect(jsonPath("$.rounds[0].matches[0].isActive").value(true))
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 PATCH 성공")
    void updateRoundsAndMatches_patch_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID teamA1 = UUID.randomUUID();
        UUID teamA2 = UUID.randomUUID();
        UUID teamB1 = UUID.randomUUID();
        UUID teamB2 = UUID.randomUUID();

        when(freeGameService.updateFreeGameRoundMatch(any(UUID.class), eq(gameId), any()))
                .thenReturn(new UpdateFreeGameRoundMatchResponse(gameId));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
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

        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(userId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("자유게임 참가자 목록 조회 성공 (include=stats)")
    void get_free_game_participants_with_stats_success() throws Exception {
        // given
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID participantUserId = UUID.randomUUID();
        FreeGameParticipantResponse participant = participantResponse(UUID.randomUUID(), participantUserId, "KimA");
        FreeGameParticipantsResponse response =
                participantsResponse(gameId, List.of(participantResponseWithStats(participant, 3, 2, 1, 1)));

        when(freeGameService.getFreeGameParticipants(userId, gameId, true))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/participants", gameId)
                        .queryParam("include", "stats")
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.participants[0].assignedMatchCount").value(3))
                .andExpect(jsonPath("$.participants[0].completedMatchCount").value(2))
                .andExpect(jsonPath("$.participants[0].winCount").value(1))
                .andExpect(jsonPath("$.participants[0].lossCount").value(1));
    }

    @Test
    @DisplayName("자유게임 참가자 목록 조회 성공 (include 없음)")
    void get_free_game_participants_without_stats_success() throws Exception {
        // given
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID participantUserId = UUID.randomUUID();
        FreeGameParticipantResponse participant = participantResponse(UUID.randomUUID(), participantUserId, "KimA");
        FreeGameParticipantsResponse response = participantsResponse(gameId, List.of(participant));

        when(freeGameService.getFreeGameParticipants(userId, gameId, false))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/participants", gameId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.participants[0].assignedMatchCount").doesNotExist())
                .andExpect(jsonPath("$.participants[0].completedMatchCount").doesNotExist())
                .andExpect(jsonPath("$.participants[0].winCount").doesNotExist())
                .andExpect(jsonPath("$.participants[0].lossCount").doesNotExist());
    }

    @Test
    @DisplayName("자유게임 참가자 상세 조회 성공")
    void get_free_game_participant_detail_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        UUID participantUserId = UUID.randomUUID();

        FreeGameParticipantDetailResponse response =
                participantDetailResponse(gameId, participantId, participantUserId, "KimA");

        when(freeGameService.getFreeGameParticipantDetail(userId, gameId, participantId))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/participants/{participantId}", gameId, participantId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.participantId").value(participantId.toString()))
                .andExpect(jsonPath("$.userId").value(participantUserId.toString()))
                .andExpect(jsonPath("$.displayName").value("KimA"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.grade").value("초심"))
                .andExpect(jsonPath("$.ageGroup").value(30));
    }

    @Test
    @DisplayName("자유게임 참가자 상세 조회 시 존재하지 않는 participantId면 실패")
    void get_free_game_participant_detail_with_unknown_participant_then_not_found() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        when(freeGameService.getFreeGameParticipantDetail(userId, gameId, participantId))
                .thenThrow(new NotFoundException("존재하지 않는 참가자입니다. participantId: " + participantId));

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/participants/{participantId}", gameId, participantId)
                        .with(authenticatedUser(userId))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").value("존재하지 않는 참가자입니다. participantId: " + participantId));
    }

    @Test
    @DisplayName("자유게임 생성 요청을 새 usec case로 전달한다.")
    void createFreeGame_delegatesToCreateUseCase() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();

        CreateFreeGameRequest request = new CreateFreeGameRequest(
                "수요 자유게임",
                null,
                GradeType.NATIONAL,
                2,
                1,
                "잠실 배드민턴장",
                List.of(managerId),
                List.of(new CreateFreeGameRequest.ParticipantRequest(
                        "p1",
                        null,
                        "서승재",
                        Gender.MALE,
                        Grade.SS,
                        20
                )),
                List.of(new CreateFreeGameRequest.RoundRequest(
                        1,
                        List.of(new CreateFreeGameRequest.CourtRequest(
                                1,
                                Arrays.asList("p1", null, null, null)
                        ))
                ))
        );

        CreateFreeGameCommand command = new CreateFreeGameCommand(
                "수요 자유게임",
                MatchRecordMode.STATUS_ONLY,
                GradeType.NATIONAL,
                2,
                1,
                "잠실 배드민턴장",
                List.of(managerId),
                List.of(new CreateFreeGameCommand.Participant(
                        "p1",
                        null,
                        "서승재",
                        Gender.MALE,
                        Grade.SS,
                        20
                )),
                List.of(new CreateFreeGameCommand.Round(
                        1,
                        List.of(new CreateFreeGameCommand.Court(
                                1,
                                Arrays.asList("p1", null, null, null)
                        ))
                ))
        );

        given(createFreeGameCommandMapper.toCommand(request))
                .willReturn(command);
        given(createFreeGameUseCase.create(userId, command))
                .willReturn(gameId);

        // when & then
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
}
