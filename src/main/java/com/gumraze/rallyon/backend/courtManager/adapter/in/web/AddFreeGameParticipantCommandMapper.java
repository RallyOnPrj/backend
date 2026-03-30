package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
import com.gumraze.rallyon.backend.courtManager.dto.AddFreeGameParticipantRequest;
import org.springframework.stereotype.Component;

@Component
public class AddFreeGameParticipantCommandMapper {

    public AddFreeGameParticipantCommand toCommand(AddFreeGameParticipantRequest request) {
        return new AddFreeGameParticipantCommand(
                request.accountId(),
                request.name(),
                request.gender(),
                request.grade(),
                request.age()
        );
    }
}
