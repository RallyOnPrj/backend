package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.IssueShareCodePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadUserPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGameSettingPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.CourtAssignment;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.RoundAssignment;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import com.gumraze.rallyon.backend.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CreateFreeGameServiceTest {

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private IssueShareCodePort issueShareCodePort;

    @Mock
    private SaveFreeGamePort saveFreeGamePort;

    @Mock
    private SaveFreeGameSettingPort saveFreeGameSettingPort;

    @Mock
    private SaveGameParticipantPort saveGameParticipantPort;

    @Mock
    private SaveFreeGameRoundPort saveFreeGameRoundPort;

    @InjectMocks
    private CreateFreeGameService createFreeGameService;

    @Test
    @DisplayName("자유게임 생성 시 shareCode, setting, 참가자, 코트 배정을 함께 처리한다")
    void createFreeGame_savesShareCodeSettingParticipantsAndRoundAssignments() {
        // given
        UUID organizerId = UUID.randomUUID();
        User organizer = User.builder().id(organizerId).build();
        String shareCode = "share-code-123";
        UUID gameId = UUID.randomUUID();
        GameParticipant p1 = GameParticipant.builder().id(UUID.randomUUID()).originalName("서승재").build();
        GameParticipant p2 = GameParticipant.builder().id(UUID.randomUUID()).originalName("김원호").build();
        GameParticipant p3 = GameParticipant.builder().id(UUID.randomUUID()).originalName("안세영").build();
        GameParticipant p4 = GameParticipant.builder().id(UUID.randomUUID()).originalName("정나은").build();

        CreateFreeGameCommand command = createCommand(
                MatchRecordMode.RESULT,
                null,
                List.of("p1", "p2", "p3", "p4"),
                null
        );

        FreeGame savedGame = FreeGame.builder()
                .id(gameId)
                .title("수요 자유게임")
                .organizer(organizer)
                .shareCode(shareCode)
                .build();

        given(loadUserPort.loadById(organizerId)).willReturn(Optional.of(organizer));
        given(issueShareCodePort.issue()).willReturn(shareCode);
        given(saveFreeGamePort.save(any())).willReturn(savedGame);
        given(saveGameParticipantPort.saveAll(any(), any())).willReturn(Map.of(
                "p1", p1,
                "p2", p2,
                "p3", p3,
                "p4", p4
        ));

        // when
        UUID createdGameId = createFreeGameService.create(organizerId, command);

        // then
        assertThat(createdGameId).isEqualTo(gameId);
        then(issueShareCodePort).should().issue();

        ArgumentCaptor<FreeGame> freeGameCaptor = ArgumentCaptor.forClass(FreeGame.class);
        then(saveFreeGamePort).should().save(freeGameCaptor.capture());
        FreeGame freeGame = freeGameCaptor.getValue();
        assertThat(freeGame.getOrganizer()).isEqualTo(organizer);
        assertThat(freeGame.getShareCode()).isEqualTo(shareCode);
        assertThat(freeGame.getGameType()).isEqualTo(GameType.FREE);
        assertThat(freeGame.getGameStatus()).isEqualTo(GameStatus.NOT_STARTED);
        assertThat(freeGame.getMatchRecordMode()).isEqualTo(MatchRecordMode.RESULT);
        assertThat(freeGame.getLocation()).isEqualTo("잠실 배드민턴장");

        then(saveFreeGameSettingPort).should().save(savedGame, 1, 1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoundAssignment>> roundAssignmentsCaptor =
                (ArgumentCaptor<List<RoundAssignment>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        then(saveFreeGameRoundPort).should().saveAll(any(), roundAssignmentsCaptor.capture());
        List<RoundAssignment> roundAssignments = roundAssignmentsCaptor.getValue();
        assertThat(roundAssignments).hasSize(1);
        CourtAssignment courtAssignment = roundAssignments.getFirst().courts().getFirst();
        assertThat(courtAssignment.slot1ParticipantId()).isEqualTo(p1.getId());
        assertThat(courtAssignment.slot2ParticipantId()).isEqualTo(p2.getId());
        assertThat(courtAssignment.slot3ParticipantId()).isEqualTo(p3.getId());
        assertThat(courtAssignment.slot4ParticipantId()).isEqualTo(p4.getId());
    }

    @Test
    @DisplayName("자유게임 생성 시 matchRecordMode가 null이면 STATUS_ONLY를 사용한다")
    void createFreeGame_withNullMatchRecordMode_defaultsToStatusOnly() {
        // given
        UUID organizerId = UUID.randomUUID();
        User organizer = User.builder().id(organizerId).build();
        given(loadUserPort.loadById(organizerId)).willReturn(Optional.of(organizer));
        given(issueShareCodePort.issue()).willReturn("share-code");
        given(saveFreeGamePort.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(saveGameParticipantPort.saveAll(any(), any())).willReturn(savedParticipantsByClientId());

        // when
        createFreeGameService.create(organizerId, createCommand(null, null, List.of("p1", "p2", "p3", "p4"), null));

        // then
        ArgumentCaptor<FreeGame> freeGameCaptor = ArgumentCaptor.forClass(FreeGame.class);
        then(saveFreeGamePort).should().save(freeGameCaptor.capture());
        assertThat(freeGameCaptor.getValue().getMatchRecordMode()).isEqualTo(MatchRecordMode.STATUS_ONLY);
    }

    @Test
    @DisplayName("managerIds가 2명을 초과하면 예외가 발생한다")
    void createFreeGame_withTooManyManagers_throws() {
        // given
        UUID organizerId = UUID.randomUUID();
        UUID managerId1 = UUID.randomUUID();
        UUID managerId2 = UUID.randomUUID();
        UUID managerId3 = UUID.randomUUID();
        User organizer = User.builder().id(organizerId).build();
        given(loadUserPort.loadById(organizerId)).willReturn(Optional.of(organizer));

        CreateFreeGameCommand command = createCommand(
                MatchRecordMode.STATUS_ONLY,
                List.of(managerId1, managerId2, managerId3),
                List.of("p1", "p2", "p3", "p4"),
                null
        );

        // when & then
        assertThatThrownBy(() -> createFreeGameService.create(organizerId, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("managerIds");

        then(saveFreeGamePort).should(never()).save(any());
    }

    @Test
    @DisplayName("organizer가 managerIds에 포함되면 예외가 발생한다")
    void createFreeGame_withOrganizerInManagerIds_throws() {
        // given
        UUID organizerId = UUID.randomUUID();
        User organizer = User.builder().id(organizerId).build();
        given(loadUserPort.loadById(organizerId)).willReturn(Optional.of(organizer));

        CreateFreeGameCommand command = createCommand(
                MatchRecordMode.STATUS_ONLY,
                List.of(organizerId),
                List.of("p1", "p2", "p3", "p4"),
                null
        );

        // when & then
        assertThatThrownBy(() -> createFreeGameService.create(organizerId, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("managerIds");

        then(saveFreeGamePort).should(never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 managerId가 있으면 예외가 발생한다")
    void createFreeGame_withUnknownManager_throws() {
        // given
        UUID organizerId = UUID.randomUUID();
        UUID unknownManagerId = UUID.randomUUID();
        User organizer = User.builder().id(organizerId).build();
        given(loadUserPort.loadById(organizerId)).willReturn(Optional.of(organizer));
        given(loadUserPort.loadById(unknownManagerId)).willReturn(Optional.empty());

        CreateFreeGameCommand command = createCommand(
                MatchRecordMode.STATUS_ONLY,
                List.of(unknownManagerId),
                List.of("p1", "p2", "p3", "p4"),
                null
        );

        // when & then
        assertThatThrownBy(() -> createFreeGameService.create(organizerId, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("managerId");

        then(saveFreeGamePort).should(never()).save(any());
    }

    @Test
    @DisplayName("slot에 null이 있어도 라운드 배정을 생성할 수 있다")
    void createFreeGame_withNullSlots_allowsSparseRoundAssignment() {
        // given
        UUID organizerId = UUID.randomUUID();
        User organizer = User.builder().id(organizerId).build();
        String shareCode = "share-code-123";
        UUID gameId = UUID.randomUUID();
        GameParticipant p1 = GameParticipant.builder().id(UUID.randomUUID()).originalName("서승재").build();

        CreateFreeGameCommand command = createCommand(
                MatchRecordMode.STATUS_ONLY,
                null,
                Arrays.asList("p1", null, null, null),
                null
        );

        FreeGame savedGame = FreeGame.builder()
                .id(gameId)
                .title("수요 자유게임")
                .organizer(organizer)
                .shareCode(shareCode)
                .build();

        given(loadUserPort.loadById(organizerId)).willReturn(Optional.of(organizer));
        given(issueShareCodePort.issue()).willReturn(shareCode);
        given(saveFreeGamePort.save(any())).willReturn(savedGame);
        given(saveGameParticipantPort.saveAll(any(), any())).willReturn(Map.of("p1", p1));

        // when
        createFreeGameService.create(organizerId, command);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoundAssignment>> roundAssignmentsCaptor =
                (ArgumentCaptor<List<RoundAssignment>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        then(saveFreeGameRoundPort).should().saveAll(any(), roundAssignmentsCaptor.capture());
        CourtAssignment courtAssignment = roundAssignmentsCaptor.getValue()
                .getFirst()
                .courts()
                .getFirst();

        assertThat(courtAssignment.slot1ParticipantId()).isEqualTo(p1.getId());
        assertThat(courtAssignment.slot2ParticipantId()).isNull();
        assertThat(courtAssignment.slot3ParticipantId()).isNull();
        assertThat(courtAssignment.slot4ParticipantId()).isNull();
    }

    private CreateFreeGameCommand createCommand(
            MatchRecordMode matchRecordMode,
            List<UUID> managerIds,
            List<String> slots,
            UUID participantUserId
    ) {
        return new CreateFreeGameCommand(
                "수요 자유게임",
                matchRecordMode,
                GradeType.NATIONAL,
                1,
                1,
                "잠실 배드민턴장",
                managerIds,
                List.of(
                        new CreateFreeGameCommand.Participant(
                                "p1",
                                participantUserId,
                                "서승재",
                                Gender.MALE,
                                Grade.A,
                                20
                        ),
                        new CreateFreeGameCommand.Participant(
                                "p2",
                                null,
                                "김원호",
                                Gender.MALE,
                                Grade.A,
                                20
                        ),
                        new CreateFreeGameCommand.Participant(
                                "p3",
                                null,
                                "안세영",
                                Gender.FEMALE,
                                Grade.A,
                                20
                        ),
                        new CreateFreeGameCommand.Participant(
                                "p4",
                                null,
                                "정나은",
                                Gender.FEMALE,
                                Grade.A,
                                20
                        )
                ),
                List.of(
                        new CreateFreeGameCommand.Round(
                                1,
                                List.of(
                                        new CreateFreeGameCommand.Court(1, slots)
                                )
                        )
                )
        );
    }

    private Map<String, GameParticipant> savedParticipantsByClientId() {
        return Map.of(
                "p1", savedParticipant("서승재"),
                "p2", savedParticipant("김원호"),
                "p3", savedParticipant("안세영"),
                "p4", savedParticipant("정나은")
        );
    }

    private GameParticipant savedParticipant(String originalName) {
        return GameParticipant.builder()
                .id(UUID.randomUUID())
                .originalName(originalName)
                .displayName(originalName)
                .build();
    }
}
