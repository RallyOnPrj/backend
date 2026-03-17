package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadUserPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class CreateFreeGameServiceTest {

    @Mock
    LoadUserPort loadUserPort;

    @Mock
    SaveFreeGamePort saveFreeGamePort;

    @Mock
    SaveGameParticipantPort saveGameParticipantPort;

    @Mock
    SaveFreeGameRoundPort saveFreeGameRoundPort;

    @InjectMocks
    CreateFreeGameService createFreeGameService;


    @Test
    @DisplayName("자유게임 생성 시 clientId 기준으로 참가자와 코트 배정을 함께 처리한다.")
    void createFreeGame_savesParticipantsAndRoundAssignments() {
        // given
        Long organizerId = 1L;
        User organizer = User.builder().id(organizerId).build();

        CreateFreeGameCommand command = new CreateFreeGameCommand(
                "수요 자유게임",
                MatchRecordMode.RESULT,
                GradeType.NATIONAL,
                1,
                1,
                null,
                null,
                List.of(
                        new CreateFreeGameCommand.Participant(
                                "p1",
                                null,
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
                                Gender.MALE,
                                Grade.A,
                                20
                        ),
                        new CreateFreeGameCommand.Participant(
                                "p4",
                                null,
                                "정나은",
                                Gender.MALE,
                                Grade.A,
                                20
                        )
                ),
                List.of(
                        new CreateFreeGameCommand.Round(
                                1,
                                List.of(
                                        new CreateFreeGameCommand.Court(
                                                1,
                                                List.of("p1", "p2", "p3", "p4")
                                        )
                                )
                        )
                )
        );


        FreeGame savedGame = FreeGame.builder()
                .id(UUID.randomUUID())
                .title("수요 자유게임")
                .build();

        given(loadUserPort.loadById(organizerId))
                .willReturn(Optional.of(organizer));
        given(saveFreeGamePort.save(any()))
                .willReturn(savedGame);
        given(saveGameParticipantPort.saveAll(any(), any()))
                .willReturn(Map.of(
                        "p1", GameParticipant.builder().id(UUID.randomUUID()).originalName("서승재").build(),
                        "p2", GameParticipant.builder().id(UUID.randomUUID()).originalName("김원호").build(),
                        "p3", GameParticipant.builder().id(UUID.randomUUID()).originalName("안세영").build(),
                        "p4", GameParticipant.builder().id(UUID.randomUUID()).originalName("정나은").build()
                ));

        // when
        createFreeGameService.create(organizerId, command);

        // then
        ArgumentCaptor<List> roundAssignmentCaptor = ArgumentCaptor.forClass(List.class);
        then(saveFreeGameRoundPort).should().saveAll(any(), roundAssignmentCaptor.capture());

        assertThat(roundAssignmentCaptor.getValue()).hasSize(1);
    }
}
