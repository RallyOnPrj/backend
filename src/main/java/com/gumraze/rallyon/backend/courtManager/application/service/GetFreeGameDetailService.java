package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameDetailUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameDetailQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameSettingPort;
import com.gumraze.rallyon.backend.courtManager.application.support.FreeGameAccessSupport;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFreeGameDetailService implements GetFreeGameDetailUseCase {

    private final LoadFreeGamePort loadFreeGamePort;
    private final LoadFreeGameSettingPort loadFreeGameSettingPort;

    @Override
    public FreeGameDetailResponse get(GetFreeGameDetailQuery query) {
        FreeGame freeGame = FreeGameAccessSupport.loadOrganizerGame(
                loadFreeGamePort,
                query.organizerId(),
                query.gameId()
        );

        FreeGameSetting setting = loadFreeGameSettingPort.loadSettingByGameId(query.gameId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게임 세팅입니다. gameId: " + query.gameId()));

        return FreeGameDetailResponse.from(freeGame, setting);
    }
}
