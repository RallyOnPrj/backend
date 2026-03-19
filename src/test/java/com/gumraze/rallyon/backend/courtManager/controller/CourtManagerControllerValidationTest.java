package com.gumraze.rallyon.backend.courtManager.controller;

import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.rallyon.backend.config.SecurityConfig;
import com.gumraze.rallyon.backend.courtManager.application.port.in.CreateFreeGameUseCase;
import com.gumraze.rallyon.backend.courtManager.dto.*;
import com.gumraze.rallyon.backend.courtManager.service.FreeGameService;
import com.gumraze.rallyon.backend.user.constants.Gender;
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
    private CreateFreeGameUseCase createFreeGameUseCase;

    @MockitoBean
    private CreateFreeGameCommandMapper createFreeGameCommandMapper;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @BeforeEach
    void setUp() {
        when(jwtAccessTokenValidator.validateAndGetUserId("token")).thenReturn(Optional.of(1L));
    }

    @Test
    @DisplayName("자유게임 생성 시 title 누락이면 400")
    void createFreeGame_without_title() throws Exception {
        assertCreateFreeGameBadRequest(createRequest(
                null, GradeType.NATIONAL, 2, 3, null, List.of(), List.of()
        ));
    }

    @Test
    @DisplayName("자유게임 생성 시 courtCount 누락이면 400")
    void createFreeGame_without_courtCount() throws Exception {
        assertCreateFreeGameBadRequest(createRequest(
                "테스트 게임", GradeType.NATIONAL, null, 3, null, List.of(), List.of()
        ));
    }

    @Test
    @DisplayName("자유게임 생성 시 roundCount 누락이면 400")
    void createFreeGame_without_roundCount() throws Exception {
        assertCreateFreeGameBadRequest(createRequest(
                "테스트 게임", GradeType.NATIONAL, 2, null, null, List.of(), List.of()
        ));
    }

    @Test
    @DisplayName("자유게임 생성 시 courtCount와 roundCount가 모두 누락이면 400")
    void createFreeGame_without_courtCount_and_roundCount() throws Exception {
        assertCreateFreeGameBadRequest(createRequest(
                "자유게임 1", GradeType.NATIONAL, null, null, null, List.of(), List.of()
        ));
    }

    @Test
    @DisplayName("자유게임 생성 시 gradeType 누락이면 400")
    void createFreeGame_without_gradeType() throws Exception {
        assertCreateFreeGameBadRequest(createRequest(
                "자유게임", null, 2, 3, null, List.of(), List.of()
        ));
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
        assertCreateFreeGameBadRequest(createRequest(
                "자유게임 1",
                GradeType.NATIONAL,
                2,
                3,
                null,
                List.of(new CreateFreeGameRequest.ParticipantRequest(
                        "p1",
                        null,
                        "참가자 1",
                        null,
                        Grade.ROOKIE,
                        20
                )),
                List.of()
        ));
    }

    @Test
    @DisplayName("자유게임 생성 시 participant clientId 누락이면 400")
    void createFreeGame_with_participant_without_clientId() throws Exception {
        assertCreateFreeGameBadRequest(createRequest(
                "자유게임 1",
                GradeType.NATIONAL,
                2,
                3,
                null,
                List.of(new CreateFreeGameRequest.ParticipantRequest(
                        null,
                        null,
                        "참가자 1",
                        Gender.MALE,
                        Grade.ROOKIE,
                        20
                )),
                List.of()
        ));
    }

    @Test
    @DisplayName("자유게임 생성 시 location 길이가 255자를 초과하면 400")
    void createFreeGame_with_too_long_location() throws Exception {
        assertCreateFreeGameBadRequest(createRequest(
                "자유게임", GradeType.NATIONAL, 2, 3, "A".repeat(256), List.of(), List.of()
        ));
    }

    @Test
    @DisplayName("자유게임 생성 시 court slots 길이가 4가 아니면 400")
    void createFreeGame_with_invalid_slots_length() throws Exception {
        assertCreateFreeGameBadRequest(createRequest(
                "자유게임",
                GradeType.NATIONAL,
                2,
                3,
                null,
                List.of(),
                List.of(
                        new CreateFreeGameRequest.RoundRequest(
                                1,
                                List.of(new CreateFreeGameRequest.CourtRequest(
                                        1,
                                        List.of("p1", "p2", "p3")
                                ))
                        )
                )
        ));
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

    private CreateFreeGameRequest createRequest(
            String title,
            GradeType gradeType,
            Integer courtCount,
            Integer roundCount,
            String location,
            List<CreateFreeGameRequest.ParticipantRequest> participants,
            List<CreateFreeGameRequest.RoundRequest> rounds
    ) {
        return new CreateFreeGameRequest(
                title,
                null,
                gradeType,
                courtCount,
                roundCount,
                location,
                null,
                participants,
                rounds
        );
    }
}
