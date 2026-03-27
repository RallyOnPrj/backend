package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.UpdateFreeGameInfoUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameInfoCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.support.FreeGameAccessSupport;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateFreeGameInfoService implements UpdateFreeGameInfoUseCase {

    private final LoadFreeGamePort loadFreeGamePort;
    private final SaveFreeGamePort saveFreeGamePort;

    @Override
    public UpdateFreeGameResponse update(UpdateFreeGameInfoCommand command) {
        FreeGame freeGame = FreeGameAccessSupport.loadOrganizerGame(
                loadFreeGamePort,
                command.organizerId(),
                command.gameId()
        );

        if (command.managerIds() != null) {
            throw new UnsupportedOperationException("매니저 수정 기능은 현재 미개발 상태입니다.");
        }

        freeGame.update(
                command.title(),
                command.matchRecordMode(),
                command.gradeType(),
                command.location()
        );

        saveFreeGamePort.save(freeGame);
        return UpdateFreeGameResponse.from(freeGame);
    }
}
