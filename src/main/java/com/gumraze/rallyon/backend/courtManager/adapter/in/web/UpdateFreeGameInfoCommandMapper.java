package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameInfoCommand;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UpdateFreeGameInfoCommandMapper {

    public UpdateFreeGameInfoCommand toCommand(UUID organizerId, UUID gameId, UpdateFreeGameRequest request) {
        return new UpdateFreeGameInfoCommand(
                organizerId,
                gameId,
                request.title(),
                request.matchRecordMode(),
                request.gradeType(),
                request.scheduledAt(),
                request.location(),
                request.managerIds()
        );
    }
}
