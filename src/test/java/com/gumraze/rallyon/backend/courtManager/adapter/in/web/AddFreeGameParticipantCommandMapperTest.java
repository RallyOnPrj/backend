package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
import com.gumraze.rallyon.backend.courtManager.dto.AddFreeGameParticipantRequest;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AddFreeGameParticipantCommandMapperTest {

    private final AddFreeGameParticipantCommandMapper mapper = new AddFreeGameParticipantCommandMapper();

    @Test
    @DisplayName("참가자 추가 request를 command로 변환한다")
    void toCommand_maps_request_to_command() {
        UUID accountId = UUID.randomUUID();
        AddFreeGameParticipantRequest request = new AddFreeGameParticipantRequest(
                accountId,
                "서승재",
                Gender.MALE,
                Grade.A,
                20
        );

        AddFreeGameParticipantCommand command = mapper.toCommand(request);

        assertThat(command.accountId()).isEqualTo(accountId);
        assertThat(command.name()).isEqualTo("서승재");
        assertThat(command.gender()).isEqualTo(Gender.MALE);
        assertThat(command.grade()).isEqualTo(Grade.A);
        assertThat(command.age()).isEqualTo(20);
    }
}
