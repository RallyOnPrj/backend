package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.AddGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddFreeGameParticipantServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private AddGameParticipantPort addGameParticipantPort;

    @InjectMocks
    private AddFreeGameParticipantService service;

    @Test
    @DisplayName("운영자는 자신의 자유게임에 참가자 1명을 추가할 수 있다.")
    void add_addsParticipantToOwnedGame() {
        // given
        // 운영자가 특정 FreeGame에 GameParticipant를 추가함.
        // 성공하면 새 참가자 정보가 있어야함.
        UUID organizerId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        // 게임 운영자
        User organizer = User.builder().id(organizerId).build();

        // 게임
        FreeGame freeGame = FreeGame.builder()
                .id(gameId)
                .title("자유게임")
                .organizer(organizer)
                .gradeType(GradeType.NATIONAL)
                .matchRecordMode(MatchRecordMode.RESULT)
                .shareCode("share-code")
                .location("숙지배드민턴")
                .build();

        // 게임 stub
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));

        UUID participantId = UUID.randomUUID();
        AddFreeGameParticipantCommand command = new AddFreeGameParticipantCommand(
                null,
                "서승재",
                Gender.MALE,
                Grade.C,
                20
        );

        GameParticipant savedParticipant = GameParticipant.builder()
                .id(participantId)
                .freeGame(freeGame)
                .user(null)
                .originalName("서승재")
                .displayName("서승재")
                .gender(Gender.MALE)
                .grade(Grade.C)
                .ageGroup(20)
                .build();

        given(addGameParticipantPort.add(freeGame, command)).willReturn(savedParticipant);

        // when
        UUID result = service.add(organizerId, gameId, command);

        // then
        assertThat(result).isEqualTo(participantId);
        verify(loadFreeGamePort).loadGameById(gameId);
        verify(addGameParticipantPort).add(freeGame, command);
    }

    @Test
    @DisplayName("존재하지 않는 자유게임이면 예외가 발생한다.")
    void add_throwsWhenGameDoesNotExist() {
        UUID organizerId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.add(
                organizerId,
                gameId,
                new AddFreeGameParticipantCommand(null, "서승재", Gender.MALE, Grade.C, 20)
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(gameId.toString());
    }

    @Test
    @DisplayName("운영자가 아니면 참가자를 추가할 수 없다.")
    void add_throwsWhenRequesterIsNotOrganizer() {
        UUID organizerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        User organizer = User.builder().id(organizerId).build();
        FreeGame freeGame = FreeGame.builder()
                .id(gameId)
                .title("자유게임")
                .organizer(organizer)
                .gradeType(GradeType.NATIONAL)
                .matchRecordMode(MatchRecordMode.RESULT)
                .shareCode("share-code")
                .location("숙지배드민턴")
                .build();

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));

        assertThatThrownBy(() -> service.add(
                requesterId,
                gameId,
                new AddFreeGameParticipantCommand(null, "서승재", Gender.MALE, Grade.C, 20)
        ))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining(gameId.toString());
    }
}
