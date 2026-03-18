package com.gumraze.rallyon.backend.courtManager.controller;

import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.rallyon.backend.config.SecurityConfig;
import com.gumraze.rallyon.backend.courtManager.dto.*;
import com.gumraze.rallyon.backend.courtManager.service.FreeGameService;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.authenticatedUser;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
class CourtManagerControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FreeGameService freeGameService;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @BeforeEach
    void setUp() {
        when(jwtAccessTokenValidator.validateAndGetUserId("token")).thenReturn(Optional.of(1L));
    }

    @Test
    @DisplayName("자유게임 생성 시 title 누락이면 400")
    void createFreeGame_without_title() throws Exception {
        // given: title이 누락된 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title(null)
                .courtCount(2)
                .roundCount(3)
                .gradeType(GradeType.NATIONAL)
                .build();

        // when & then: validation 실패로 400을 반환해야 한다.
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시 courtCount 누락이면 400")
    void createFreeGame_without_courtCount() throws Exception {
        // given: courtCount가 누락된 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("테스트 게임")
                .gradeType(GradeType.NATIONAL)
                .roundCount(3)
                .build();

        // when & then: validation 실패로 400을 반환해야 한다.
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시 roundCount 누락이면 400")
    void createFreeGame_without_roundCount() throws Exception {
        // given: roundCount가 누락된 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("테스트 게임")
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .build();

        // when & then: validation 실패로 400을 반환해야 한다.
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시 courtCount와 roundCount가 모두 누락이면 400")
    void createFreeGame_without_courtCount_and_roundCount() throws Exception {
        // given: courtCount와 roundCount가 모두 없는 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .gradeType(GradeType.NATIONAL)
                .build();

        // when & then: validation 실패로 400을 반환해야 한다.
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시 gradeType 누락이면 400")
    void createFreeGame_without_gradeType() throws Exception {
        // given: gradeType이 누락된 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .courtCount(2)
                .roundCount(3)
                .build();

        // when & then: validation 실패로 400을 반환해야 한다.
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시 gradeType 값이 유효하지 않으면 400")
    void createFreeGame_with_invalid_gradeType() throws Exception {
        // given: 잘못된 gradeType 값이 포함된 JSON 요청을 준비한다.
        String body = """
                {
                  "title": "자유게임",
                  "gradeType": "REGION",
                  "courtCount": 2,
                  "roundCount": 3
                }
                """;

        // when & then: enum 변환 실패로 400을 반환해야 한다.
        assertCreateFreeGameBadRequest(body);
    }

    @Test
    @DisplayName("자유게임 생성 시 참가자 필수 항목이 누락되면 400")
    void createFreeGame_with_participant_without_gender() throws Exception {
        // given: 참가자는 있지만 gender가 없는 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(3)
                .participants(
                        List.of(
                                ParticipantCreateRequest.builder()
                                        .originalName("참가자 1")
                                        .grade(Grade.ROOKIE)
                                        .ageGroup(20)
                                        .build()
                        )
                )
                .build();

        // when & then: 참가자 validation 실패로 400을 반환해야 한다.
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("자유게임 생성 시 location 길이가 255자를 초과하면 400")
    void createFreeGame_with_too_long_location() throws Exception {
        // given: 길이가 255자를 초과하는 location이 포함된 생성 요청을 준비한다.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(3)
                .location("A".repeat(256))
                .build();

        // when & then: validation 실패로 400을 반환해야 한다.
        assertCreateFreeGameBadRequest(request);
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - rounds 누락이면 400")
    void updateRoundMatch_without_rounds_then_bad_request() throws Exception {
        // given: rounds가 누락된 요청을 준비한다.
        String body = "{}";
        UUID gameId = UUID.randomUUID();

        // when & then: validation 실패로 400을 반환해야 한다.
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - roundNumber 누락이면 400")
    void updateRoundMatch_without_roundNumber_then_bad_request() throws Exception {
        // given: roundNumber가 누락된 요청을 준비한다.
        UUID gameId = UUID.randomUUID();
        UUID teamA1 = UUID.randomUUID();
        UUID teamA2 = UUID.randomUUID();
        UUID teamB1 = UUID.randomUUID();
        UUID teamB2 = UUID.randomUUID();
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(null)
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

        String body = objectMapper.writeValueAsString(request);

        // when & then: validation 실패로 400을 반환해야 한다.
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - matches 누락이면 400")
    void updateRoundMatch_without_matches_then_bad_request() throws Exception {
        // given: matches가 누락된 요청을 준비한다.
        UUID gameId = UUID.randomUUID();
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(null)
                                        .build()
                        ))
                        .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then: validation 실패로 400을 반환해야 한다.
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - teamAIds 누락이면 400")
    void updateRoundMatch_without_teamAIds_then_bad_request() throws Exception {
        // given: teamAIds가 누락된 요청을 준비한다.
        UUID gameId = UUID.randomUUID();
        UUID teamB1 = UUID.randomUUID();
        UUID teamB2 = UUID.randomUUID();
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(null)
                                                        .teamBIds(List.of(teamB1, teamB2))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then: validation 실패로 400을 반환해야 한다.
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - teamBIds 누락이면 400")
    void updateRoundMatch_without_teamBIds_then_bad_request() throws Exception {
        // given: teamBIds가 누락된 요청을 준비한다.
        UUID gameId = UUID.randomUUID();
        UUID teamA1 = UUID.randomUUID();
        UUID teamA2 = UUID.randomUUID();
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(teamA1, teamA2))
                                                        .teamBIds(null)
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then: validation 실패로 400을 반환해야 한다.
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    private void assertCreateFreeGameBadRequest(CreateFreeGameRequest request) throws Exception {
        assertCreateFreeGameBadRequest(objectMapper.writeValueAsString(request));
    }

    private void assertCreateFreeGameBadRequest(String body) throws Exception {
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(1L))
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }
}
